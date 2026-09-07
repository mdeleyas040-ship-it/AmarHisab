package com.eleyas.expensetracker.repository

import com.eleyas.expensetracker.model.Birthday

class BirthdayRepository {

    private val birthdays = mutableListOf<Birthday>()

    fun getAll(): List<Birthday> {
        return birthdays.toList()
    }

    fun add(birthday: Birthday) {
        birthdays.add(birthday)
    }

    fun update(birthday: Birthday) {
        val index = birthdays.indexOfFirst { it.id == birthday.id }

        if (index != -1) {
            birthdays[index] = birthday
        }
    }

    fun delete(id: String) {
        birthdays.removeAll { it.id == id }
    }

    fun getById(id: String): Birthday? {
        return birthdays.firstOrNull { it.id == id }
    }
}