package org.krithika.Kwordle

interface Observable {
    val letterObservers: HashSet<LetterObserver>
    val controlObservers: HashSet<ControlObserver>

    fun addLetterObserver(letterObserver: LetterObserver) {
        letterObservers.add(letterObserver)
    }

    fun addControlObserver(controlObserver: ControlObserver) {
        controlObservers.add(controlObserver)
    }

    fun removeLetterObserver(letterObserver: LetterObserver) {
        letterObservers.remove(letterObserver)
    }

    fun removeControlObserver(controlObserver: ControlObserver) {
        controlObservers.remove(controlObserver)
    }

    fun sendUpdateEventLetter(ch: Char) {
        ArrayList(letterObservers).forEach { it.updateLetter(ch) }
    }

    fun sendUpdateEventControl(control: ControlObserver.Control) {
        ArrayList(controlObservers).forEach { it.updateControl(control)}
    }
}