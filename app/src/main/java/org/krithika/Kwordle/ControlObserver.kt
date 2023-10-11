package org.krithika.Kwordle

interface ControlObserver {
    enum class Control {
        Enter,
        Delete,
        ClueFinished
    }
    fun updateControl(control: Control)
}