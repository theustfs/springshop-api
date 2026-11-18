package online.springshop.api.shared.event.account;

public sealed interface AccountEvent permits
    AccountRegisteredEvent, AccountPasswordUpdateEvent,
    AccountEmailUpdateStepOneEvent, AccountEmailUpdateStepTwoEvent,
    AccountDeletionEvent
{

}
