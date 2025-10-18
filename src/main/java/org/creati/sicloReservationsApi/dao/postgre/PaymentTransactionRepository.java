package org.creati.sicloReservationsApi.dao.postgre;

import org.creati.sicloReservationsApi.dao.BaseRepository;
import org.creati.sicloReservationsApi.dao.postgre.model.PaymentTransaction;
import org.creati.sicloReservationsApi.service.model.PaymentTableDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentTransactionRepository extends BaseRepository<PaymentTransaction, Long> {

    @Query("""
                SELECT new org.creati.sicloReservationsApi.service.model.PaymentTableDto(
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
            """)
    Page<PaymentTableDto> getPaymentTable(Pageable pageable);

}
