package cc.sapphiretech.rose.models

enum class RosePermission(val value: Int) {
    MANAGE_ROLES(0),
    MANAGE_USERS(100);

    companion object {
        private val lut = entries.associateBy { it.value }
        fun fromOrd(num: Int): RosePermission? = lut[num]
    }
}