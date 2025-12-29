package cz.adamec.timotej.snag.projects.business

data class InspectorImpl(
    override val id: Int,
    override val name: String,
    override val phoneNumber: String,
) : Inspector {
    init {
        require(PhoneNumberValidator()(phoneNumber)) { "Invalid phone number: $phoneNumber" }
    }
}
