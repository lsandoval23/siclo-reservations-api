package org.creati.sicloReservationsApi.dao.postgre;

import org.creati.sicloReservationsApi.dao.BaseRepository;
import org.creati.sicloReservationsApi.dao.postgre.model.PaymentTransaction;
import org.creati.sicloReservationsApi.service.model.PaymentTableDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentTransactionRepository extends BaseRepository<PaymentTransaction, Long> {

    @Query("""
                SELECT new org.creati.sicloReservationsApi.service.model.PaymentTableDto(
                    p.operationId,
                    p.month,
                    p.day,
                    p.week,
                    p.purchaseDate,
                    p.accreditationDate,
                    p.releaseDate,
                    p.operationType,
                    p.productValue,
                    p.transactionFee,
                    p.amountReceived,
                    p.installments,
                    p.paymentMethod,
                    p.packageName,
                    p.classCount,
                    new org.creati.sicloReservationsApi.service.model.PaymentTableDto$ClientInfo(
                        c.name,
                        c.email,
                        c.phone
                    )
                )
                FROM PaymentTransaction p
                JOIN p.client c
                WHERE p.purchaseDate >= :fromDate AND p.purchaseDate <= :toDate
            """)
    Page<PaymentTableDto> getPaymentTable(
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable
    );


    @Query("""
                SELECT new org.creati.sicloReservationsApi.service.model.PaymentTableDto$PaymentTableSummary(
                    p.status,
                    COUNT(p),
                    SUM(p.amountReceived)
                )
                FROM PaymentTransaction p
                WHERE p.purchaseDate >= :fromDate AND p.purchaseDate <= :toDate
                GROUP BY p.status
            """)
    List<PaymentTableDto.PaymentTableSummary> getPaymentSummary(@Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate);

}
