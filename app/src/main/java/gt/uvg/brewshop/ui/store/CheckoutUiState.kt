package gt.uvg.brewshop.ui.store

import gt.uvg.brewshop.domain.validateBusinessName
import gt.uvg.brewshop.domain.validateFullName
import gt.uvg.brewshop.domain.validateNit
import gt.uvg.brewshop.domain.validatePhoneNumber
import gt.uvg.brewshop.model.BillingType
import gt.uvg.brewshop.model.PaymentMethod

/** Un campo de texto del formulario: lo escrito y si la persona ya lo edito. */
data class FormField(
    val value: String = "",
    val isTouched: Boolean = false
)

/**
 * El campo queda tocado en su primera edicion, que es el criterio del grupo para todo el
 * formulario. Solo cuenta si el texto cambia de verdad.
 */
fun FormField.edited(newValue: String): FormField =
    copy(value = newValue, isTouched = isTouched || newValue != value)

/**
 * Estado inmutable del checkout.
 *
 * Los errores se calculan a partir de los valores en lugar de guardarse. Asi un error viejo
 * de NIT no puede quedar almacenado y bloquear la compra en CF: en CF los errores fiscales
 * dan null por construccion, y al volver a NIT se recalculan con el texto conservado.
 */
data class CheckoutUiState(
    val fullName: FormField = FormField(),
    val phone: FormField = FormField(),
    val billingType: BillingType = BillingType.CF,
    val nit: FormField = FormField(),
    val businessName: FormField = FormField(),
    val paymentMethod: PaymentMethod = PaymentMethod.CASH_ON_DELIVERY
) {
    val isNitBilling: Boolean get() = billingType == BillingType.NIT

    val fullNameError: String? get() = validateFullName(fullName.value)

    val phoneError: String? get() = validatePhoneNumber(phone.value)

    val nitError: String? get() = if (isNitBilling) validateNit(nit.value) else null

    val businessNameError: String?
        get() = if (isNitBilling) validateBusinessName(businessName.value) else null

    // Lo que se pinta en rojo: el error solo aparece cuando el campo ya se edito.
    val visibleFullNameError: String? get() = fullNameError.takeIf { fullName.isTouched }

    val visiblePhoneError: String? get() = phoneError.takeIf { phone.isTouched }

    val visibleNitError: String? get() = nitError.takeIf { nit.isTouched }

    val visibleBusinessNameError: String?
        get() = businessNameError.takeIf { businessName.isTouched }

    /**
     * Se calcula siempre, tambien con campos sin tocar: un formulario vacio es invalido
     * aunque no muestre nada en rojo. El metodo de pago siempre tiene un valor.
     */
    val isFormValid: Boolean
        get() = fullNameError == null &&
            phoneError == null &&
            nitError == null &&
            businessNameError == null
}
