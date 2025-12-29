package cz.adamec.timotej.snag.projects.business

class PhoneNumberValidator {
    operator fun invoke(phoneNumber: String) =
        phoneNumber.isNotBlank() && phoneNumber.matches(Regex("\\d{9}"))
}
