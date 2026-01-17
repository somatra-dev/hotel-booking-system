package co.istad.telegram.state;

public enum ConversationState {

    NONE,

    // Booking flow states
    BOOKING_SELECT_ROOM_TYPE,
    BOOKING_ENTER_CHECK_IN,
    BOOKING_ENTER_CHECK_OUT,
    BOOKING_ENTER_NAME,
    BOOKING_ENTER_EMAIL,
    BOOKING_ENTER_PHONE,
    BOOKING_CONFIRM,

    // My bookings flow
    MY_BOOKINGS_ENTER_PHONE,

    // Cancel flow
    CANCEL_ENTER_BOOKING_ID,
    CANCEL_ENTER_PHONE,

    // Login flow
    LOGIN_ENTER_EMAIL,
    LOGIN_ENTER_PASSWORD,

    // Staff operations
    STAFF_SEARCH_ENTER_QUERY
}
