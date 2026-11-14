package com.concessionaria.enums;

public enum Role {
    CUSTOMER,
    OPERATOR,
    ADMIN;

    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}

