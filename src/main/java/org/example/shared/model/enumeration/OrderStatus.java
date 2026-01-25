package org.example.shared.model.enumeration;

public enum OrderStatus {
    PENDING(0, "En attente"),
    PAID(1, "Payée"),
    SHIPPED(2, "Expédiée"),
    DELIVERED(3, "Livrée"),
    CANCELED(4, "Annulée");

    private final int code;
    private final String label;

    OrderStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() { return code; }
    public String getLabel() { return label; }

    // Méthode pour récupérer l'Enum à partir de l'int de la DB
    public static OrderStatus fromCode(int code) {
        for (OrderStatus s : values()) {
            if (s.code == code) return s;
        }
        return PENDING;
    }
}
