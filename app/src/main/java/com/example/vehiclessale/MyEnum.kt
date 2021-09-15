package com.example.vehiclessale

enum class MyEnum {
    REGISTER {
        override fun Name(): String { return "Register"}
    },
    SALER {
        override fun Name(): String { return "Seller"}
    },
    BUYER {
        override fun Name(): String {return "Customer"}
    },
    ADD_PRODUCT {
        override fun Name(): String {return "Add Product"}
    },
    lOADING {
        override fun Name(): String {return "Loading"}
    },
    SUCCESS {
        override fun Name(): String {return "Success"}
    },
    FAIL {
        override fun Name(): String {return "Fail"}
    },
    TO_SHIP {
        override fun Name(): String {return "To Pay"}
    },
    POST_NEW {
        override fun Name(): String {return "New"}
    },
    ADDED_CART {
        override fun Name(): String {return "Added Cart"}
    },
    DELIVERING {
        override fun Name(): String {return "Delivering"}
    },
    CONFIRMATION {
        override fun Name(): String {return "To Confirm"}
    },
    WAIT_CONFIRM {
        override fun Name(): String {return "Wait Confirm"}
    },
    COMPLETE {
        override fun Name(): String {return "Complete"}
    },
    NO_SEEN {
        override fun Name(): String {return "no seen"}
    },
    SEEN {
        override fun Name(): String {return "seen"}
    },
    OTHER {
        override fun Name(): String {return "Other"}
    };

    abstract fun Name(): String
}