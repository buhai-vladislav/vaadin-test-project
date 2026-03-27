package com.testtask.ui.dashboard.components.toolbar

import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.value.ValueChangeMode

object FilterItem {
    fun build(placeholder: String, field: TextField, onClick: () -> Unit): TextField {
        field.placeholder = placeholder
        field.isClearButtonVisible = true
        field.valueChangeMode = ValueChangeMode.LAZY
        field.addValueChangeListener { onClick() }

        return field
    }
}