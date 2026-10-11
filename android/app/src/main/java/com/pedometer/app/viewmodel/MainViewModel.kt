package com.pedometer.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pedometer.app.data.db.AppDatabase
import com.pedometer.app.data.model.AppSettings
import com.pedometer.app.data.model.StepRecord
import com.pedometer.app.data.preferences.UserPreferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val preferences = UserPreferences(application)

    val settings: StateFlow<AppSettings> = preferences.settingsFlow

    private val _selectedDate = MutableStateFlow(LocalDate.now().toString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    val recentRecords: StateFlow<List<StepRecord>> = database.stepDao().observeRecentRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentRecord: StateFlow<StepRecord> = _selectedDate
        .flatMapLatest { date ->
            database.stepDao().observeRecordForDate(date)
                .map { it ?: StepRecord(date = date) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StepRecord())

    private val _currentStreak = MutableStateFlow(0)
    val currentStreak: StateFlow<Int> = _currentStreak.asStateFlow()

    init {
        viewModelScope.launch {
            combine(recentRecords, settings) { records, s ->
                calculateStreak(records, s.stepGoal)
            }.collect { streak ->
                _currentStreak.value = streak
            }
        }
    }

    fun selectDate(date: String) {
        _selectedDate.value = date
    }

    fun selectToday() {
        _selectedDate.value = LocalDate.now().toString()
    }

    private fun calculateStreak(records: List<StepRecord>, goal: Int): Int {
        if (records.isEmpty()) return 0
        val sorted = records.sortedByDescending { it.date }
        var streak = 0
        val today = LocalDate.now().toString()

        for (record in sorted) {
            if (record.stepCount >= goal) {
                streak++
            } else if (record.date == today) {
                // Today in progress, ignore failure
                continue
            } else {
                break
            }
        }
        return streak
    }

    fun getLast7Days(records: List<StepRecord>): List<StepRecord> {
        val today = LocalDate.now()
        return (6 downTo 0).map { offset ->
            val d = today.minusDays(offset.toLong()).toString()
            records.find { it.date == d } ?: StepRecord(date = d)
        }
    }
}
