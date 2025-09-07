package org.creati.sicloReservationsApi.dao.postgre.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_transaction")
@Data
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long transactionId;

    @Column(name = "month")
    private Integer month;

    @Column(name = "day")
    private Integer day;

    @Column(name = "week")
    private Integer week;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(name = "accreditation_date")
    private LocalDate accreditationDate;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Column(name = "operation_type", length = 100)
    private String operationType;

    @Column(name = "product_value", precision = 12, scale = 2)
    private BigDecimal productValue;

    @Column(name = "transaction_fee", precision = 12, scale = 2)
    private BigDecimal transactionFee;

    @Column(name = "amount_received", precision = 12, scale = 2)
    private BigDecimal amountReceived;

    @Column(name = "installments")
    private Integer installments;

    @Column(name = "payment_method", length = 100)
    private String paymentMethod;

    @Column(name = "package", length = 100)
    private String packageName;

    @Column(name = "class_count")
    private Integer classCount;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}