package org.creati.sicloReservationsApi.file.excel;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.creati.sicloReservationsApi.cache.model.EntityCache;
import org.creati.sicloReservationsApi.dao.postgre.ClientRepository;
import org.creati.sicloReservationsApi.dao.postgre.DisciplineRepository;
import org.creati.sicloReservationsApi.dao.postgre.InstructorRepository;
import org.creati.sicloReservationsApi.dao.postgre.RoomRepository;
import org.creati.sicloReservationsApi.dao.postgre.StudioRepository;
import org.creati.sicloReservationsApi.dao.postgre.model.Client;
import org.creati.sicloReservationsApi.dao.postgre.model.Discipline;
import org.creati.sicloReservationsApi.dao.postgre.model.Instructor;
import org.creati.sicloReservationsApi.dao.postgre.model.Reservation;
import org.creati.sicloReservationsApi.dao.postgre.model.Room;
import org.creati.sicloReservationsApi.dao.postgre.model.Studio;
import org.creati.sicloReservationsApi.file.excel.util.ExcelUtils;
import org.creati.sicloReservationsApi.file.model.PaymentDto;
import org.creati.sicloReservationsApi.file.model.ReservationDto;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class ExcelParser {

    public static final Map<String, String> REQUIRED_PAYMENT_COLUMNS = Map.ofEntries(
            Map.entry("MONTH", "Mes"),
            Map.entry("DAY", "dia"),
            Map.entry("WEEK", "semana"),
            Map.entry("PURCHASE_DATE", "Fecha de compra (date_created)"),
            Map.entry("ACCREDITATION_DATE", "Fecha de acreditación (date_approved)"),
            Map.entry("RELEASE_DATE", "Fecha de liberación del dinero (date_released)"),
            Map.entry("COUNTERPART_EMAIL", "E-mail de la contraparte (counterpart_email)"),
            Map.entry("COUNTERPART_PHONE", "Teléfono de la contraparte (counterpart_phone_number)"),
            Map.entry("BUYER_DOCUMENT", "Documento de la contraparte (buyer_document)"),
            Map.entry("OPERATION_TYPE", "Tipo de operación (operation_type)"),
            Map.entry("PRODUCT_VALUE", "Valor del producto (transaction_amount)"),
            Map.entry("TRANSACTION_FEE", "Tarifa de Mercado Pago (mercadopago_fee)"),
            Map.entry("AMOUNT_RECEIVED", "Monto recibido (net_received_amount)"),
            Map.entry("INSTALLMENTS", "Cuotas (installments)"),
            Map.entry("PAYMENT_TYPE", "Medio de pago (payment_type)"),
            Map.entry("PACKAGE", "paquete"),
            Map.entry("CLASS_COUNT", "N° de clases")
    );

    public static final Map<String, String> OPTIONAL_PAYMENT_COLUMNS = Map.ofEntries(
            Map.entry("EXTERNAL_REFERENCE", "Código de referencia (external_reference)"),
            Map.entry("SELLER_CUSTOM_FIELD", "SKU Producto (seller_custom_field)"),
            Map.entry("OPERATION_ID", "Número de operación de Mercado Pago (operation_id)"),
            Map.entry("STATUS", "Estado de la operación (status)"),
            Map.entry("STATUS_DETAIL", "Detalle del estado de la operación (status_detail)"),
            Map.entry("MARKETPLACE_FEE", "Comisión por uso de plataforma de terceros (marketplace_fee)"),
            Map.entry("SHIPPING_COST", "Costo de envío (shipping_cost)"),
            Map.entry("COUPON_FEE", "Descuento a tu contraparte (coupon_fee)"),
            Map.entry("AMOUNT_REFUNDED", "Monto devuelto (amount_refunded)"),
            Map.entry("REFUND_OPERATOR", "Operador que devolvió dinero (refund_operator)"),
            Map.entry("CLAIM_ID", "Número de reclamo (claim_id)"),
            Map.entry("CHARGEBACK_ID", "Número de contracargo (chargeback_id)"),
            Map.entry("MARKETPLACE", "Plataforma (marketplace)"),
            Map.entry("ORDER_ID", "Número de venta en Mercado Libre (order_id)"),
            Map.entry("MERCHANT_ORDER_ID", "Número de venta en tu negocio online (merchant_order_id)"),
            Map.entry("CAMPAIGN_ID", "Número de campaña de descuento (campaign_id)"),
            Map.entry("CAMPAIGN_NAME", "Nombre de campaña de descuento (campaign_name)"),
            Map.entry("ACTIVITY_URL", "Detalle de la venta (activity_url)"),
            Map.entry("ID", "Mercado Pago Point (id)"),
            Map.entry("SHIPMENT_STATUS", "Estado del envío (shipment_status)"),
            Map.entry("BUYER_ADDRESS", "Domicilio del comprador (buyer_address)"),
            Map.entry("TRACKING_NUMBER", "Código de seguimiento (tracking_number)"),
            Map.entry("OPERATOR_NAME", "Operador en cobros de Point (operator_name)"),
            Map.entry("STORE_ID", "Número de local (store_id)"),
            Map.entry("POS_ID", "Número de caja (pos_id)"),
            Map.entry("EXTERNAL_ID", "Número de caja externo (external_id)"),
            Map.entry("FINANCING_FEE", "Costos de financiación (financing_fee)"),
            Map.entry("CLASS_COUNT2", "N° de clases2"),
            Map.entry("OBSERVACIONES", "Obervaciones"),
            Map.entry("REASON", "Descripción de la operación (reason)")
    );

    public static final String PAYMENTS_SHEET_NAME = "M-pago";

    private final ClientRepository clientRepository;
    private final StudioRepository studioRepository;
    private final RoomRepository roomRepository;
    private final DisciplineRepository disciplineRepository;
    private final InstructorRepository instructorRepository;

    public ExcelParser(
            final ClientRepository clientRepository, final StudioRepository studioRepository,
            final RoomRepository roomRepository, final DisciplineRepository disciplineRepository,
            final InstructorRepository instructorRepository) {
        this.clientRepository = clientRepository;
        this.studioRepository = studioRepository;
        this.roomRepository = roomRepository;
        this.disciplineRepository = disciplineRepository;
        this.instructorRepository = instructorRepository;
    }

    // Method to parse reservations from the uploaded Excel file, generates DTO's only
    public List<ReservationDto> parseReservationsFromFile(MultipartFile file) {
        List<ReservationDto> reservations = new ArrayList<>();
        try (Workbook workbook = ExcelUtils.createWorkbook(file)){
            Sheet sheet = workbook.getSheetAt(0);
            if (validateReservationHeaders(sheet.getRow(0))) {
                throw new RuntimeException("Invalid Excel headers");
            }

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || ExcelUtils.isEmptyRow(row)) continue;
                try {
                    ReservationDto dto = parseReservationRow(row);
                    log.info("Parsed Reservation DTO: {} for index: {}", dto, rowIndex);
                    reservations.add(dto);
                } catch (Exception e) {
                    throw new RuntimeException("Error parsing row " + (rowIndex + 1), e);
                }
            }
        } catch (IOException exception) {
            throw new RuntimeException("Error reading Excel file: " + exception.getMessage(), exception);
        }

        return reservations;
    }

    public List<PaymentDto> parsePaymentsFromFile(MultipartFile file) {
        List<PaymentDto> payments = new ArrayList<>();
        try (Workbook workbook = ExcelUtils.createWorkbook(file)){
            Sheet sheet = workbook.getAllNames().stream()
                    .filter(name -> PAYMENTS_SHEET_NAME.equalsIgnoreCase(name.getNameName()))
                    .findFirst()
                    .map(name -> workbook.getSheetAt(name.getSheetIndex()))
                    .orElseThrow(() -> new RuntimeException("Payments sheet not found"));

            if (validatePaymentHeaders(sheet.getRow(0))) {
                throw new RuntimeException("Invalid Excel headers");
            }

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || ExcelUtils.isEmptyRow(row)) continue;
                try {
                    PaymentDto dto = parsePaymentRow(row);
                    log.info("Parsed Payment DTO: {} for index: {}", dto, rowIndex);
                    payments.add(dto);
                } catch (Exception e) {
                    throw new RuntimeException("Error parsing row " + (rowIndex + 1), e);
                }
            }
        } catch (IOException exception) {
            throw new RuntimeException("Error reading Excel file: " + exception.getMessage(), exception);
        }

        return payments;
    }




    private boolean validateReservationHeaders(Row headerRow) {
        String[] expectedHeaders = {
                "Id reserva", "Id clase", "País", "Ciudad", "Disciplina",
                "Estudio", "Salón", "Instructor", "Día", "Hora",
                "Cliente", "Creador del pedido", "Método de pago", "Estatus"
        };

        if (headerRow.getPhysicalNumberOfCells() < expectedHeaders.length) {
            return false;
        }

        for (int i = 0; i < expectedHeaders.length; i++) {
            Cell cell = headerRow.getCell(i);
            String cellValue = ExcelUtils.getCellStringValue(cell);
            if (cell == null || !expectedHeaders[i].equalsIgnoreCase(cellValue)) {
                return false;
            }
        }
        return true;
    }

    private boolean validatePaymentHeaders(Row headerRow) {
        if (Objects.isNull(headerRow)) return false;
        Map<Integer, String> foundColumns = new HashMap<>();

        for (int i = 0; i < headerRow.getPhysicalNumberOfCells(); i++) {
            Cell cell = headerRow.getCell(i);
            if (cell != null) {
                String columnName = ExcelUtils.getCellStringValue(cell).trim();
                foundColumns.put(i, columnName);
            }
        }

        log.info("Found columns: {}", foundColumns);

        for (Map.Entry<String, String> entry : REQUIRED_PAYMENT_COLUMNS.entrySet()) {
            String requiredColumnName = entry.getValue();
            boolean found = foundColumns.values().stream()
                    .anyMatch(col -> normalizeColumnName(col).equals(normalizeColumnName(requiredColumnName)));

            if (!found) {
                log.error("Missing required column: {} (normalized: {})",
                        requiredColumnName, normalizeColumnName(requiredColumnName));
                return false;
            }
        }

        return true;
    }

    private String normalizeColumnName(String columnName) {
        if (columnName == null) return "";
        return columnName.toLowerCase()
                .trim()
                .replaceAll("\\s+", " ")
                .replaceAll("[áàâã]", "a")
                .replaceAll("[éèê]", "e")
                .replaceAll("[íìî]", "i")
                .replaceAll("[óòôõ]", "o")
                .replaceAll("[úùû]", "u")
                .replaceAll("ñ", "n");
    }

    private ReservationDto parseReservationRow(Row row) {
        ReservationDto dto = new ReservationDto();
        dto.setReservationId(ExcelUtils.getCellLongValue(row.getCell(0)));
        dto.setClassId(ExcelUtils.getCellLongValue(row.getCell(1)));
        dto.setCountry(ExcelUtils.getCellStringValue(row.getCell(2)));
        dto.setCity(ExcelUtils.getCellStringValue(row.getCell(3)));
        dto.setDisciplineName(ExcelUtils.getCellStringValue(row.getCell(4)));
        dto.setStudioName(ExcelUtils.getCellStringValue(row.getCell(5)));
        dto.setRoomName(ExcelUtils.getCellStringValue(row.getCell(6)));
        dto.setInstructorName(ExcelUtils.getCellStringValue(row.getCell(7)));
        dto.setDay(ExcelUtils.getCellDateValue(row.getCell(8)));
        dto.setTime(ExcelUtils.getCellTimeValue(row.getCell(9)));
        dto.setClientEmail(ExcelUtils.getCellStringValue(row.getCell(10)));
        dto.setOrderCreator(ExcelUtils.getCellStringValue(row.getCell(11)));
        dto.setPaymentMethod(ExcelUtils.getCellStringValue(row.getCell(12)));
        dto.setStatus(ExcelUtils.getCellStringValue(row.getCell(13)));
        return dto;
    }

    private PaymentDto parsePaymentRow(Row row) {
        try {
            PaymentDto dto = new PaymentDto();
            Map<String, Integer> columnIndices = getPaymentColumnIndices(row.getSheet().getRow(0));

            dto.setMonth(ExcelUtils.getCellIntegerValueSafe(row, columnIndices.get("MONTH")));
            dto.setDay(ExcelUtils.getCellIntegerValueSafe(row, columnIndices.get("DAY")));
            dto.setWeek(ExcelUtils.getCellIntegerValueSafe(row, columnIndices.get("WEEK")));
            dto.setPurchaseDate(ExcelUtils.getCellDateValueSafe(row, columnIndices.get("PURCHASE_DATE")));
            dto.setAccreditationDate(ExcelUtils.getCellDateValueSafe(row, columnIndices.get("ACCREDITATION_DATE")));
            dto.setReleaseDate(ExcelUtils.getCellDateValueSafe(row, columnIndices.get("RELEASE_DATE")));
            dto.setClientEmail(ExcelUtils.getCellStringValueSafe(row, columnIndices.get("COUNTERPART_EMAIL")));
            dto.setPhone(ExcelUtils.getCellStringValueSafe(row, columnIndices.get("COUNTERPART_PHONE")));
            dto.setDocumentId(ExcelUtils.getCellStringValueSafe(row, columnIndices.get("BUYER_DOCUMENT")));
            dto.setOperationType(ExcelUtils.getCellStringValueSafe(row, columnIndices.get("OPERATION_TYPE")));
            dto.setProductValue(ExcelUtils.getCellBigDecimalValueSafe(row, columnIndices.get("PRODUCT_VALUE")));
            dto.setTransactionFee(ExcelUtils.getCellBigDecimalValueSafe(row, columnIndices.get("TRANSACTION_FEE")));
            dto.setAmountReceived(ExcelUtils.getCellBigDecimalValueSafe(row, columnIndices.get("AMOUNT_RECEIVED")));
            dto.setInstallments(ExcelUtils.getCellIntegerValueSafe(row, columnIndices.get("INSTALLMENTS")));
            dto.setPaymentMethod(ExcelUtils.getCellStringValueSafe(row, columnIndices.get("PAYMENT_TYPE")));
            dto.setPackageName(ExcelUtils.getCellStringValueSafe(row, columnIndices.get("PACKAGE")));
            dto.setClassCount(ExcelUtils.getCellIntegerValueSafe(row, columnIndices.get("CLASS_COUNT")));

            return dto;

        } catch (Exception e) {
            throw new RuntimeException("Error parsing payment row at index " + row.getRowNum(), e);
        }
    }

    private Map<String, Integer> getPaymentColumnIndices(Row headerRow) {
        Map<String, Integer> columnIndices = new HashMap<>();

        if (headerRow == null) {
            return columnIndices;
        }

        Map<String, String> allColumns = new HashMap<>();
        allColumns.putAll(REQUIRED_PAYMENT_COLUMNS);
        allColumns.putAll(OPTIONAL_PAYMENT_COLUMNS);

        for (Map.Entry<String, String> entry : allColumns.entrySet()) {
            String key = entry.getKey();
            String expectedColumnName = entry.getValue();

            for (int i = 0; i < headerRow.getPhysicalNumberOfCells(); i++) {
                Cell cell = headerRow.getCell(i);
                if (cell != null) {
                    String actualColumnName = ExcelUtils.getCellStringValue(cell).trim();

                    if (normalizeColumnName(actualColumnName).equals(normalizeColumnName(expectedColumnName))) {
                        columnIndices.put(key, i);
                        log.info("Mapped column '{}' to index {}", key, i);
                        break;
                    }
                }
            }
        }

        log.info("Final column indices mapping: {}", columnIndices);
        return columnIndices;
    }


    public Reservation buildReservationFromDto(ReservationDto dto, EntityCache cache ){

        Client newClient = cache.getClientsByEmail().computeIfAbsent(dto.getClientEmail(), email -> {
            log.info("Creating new client for email: {}", email);
            Client client = Client.builder()
                    .email(email)
                    .build();
            return clientRepository.save(client);
        });

        Studio newStudio = cache.getStudiosByName().computeIfAbsent(dto.getStudioName(), name -> {
            log.info("Creating new studio for name: {}", name);
            Studio studio = Studio.builder()
                    .name(name)
                    .build();
            return studioRepository.save(studio);
        });

        String roomKey = newStudio.getName() + "|" + dto.getRoomName();
        Room newRoom = cache.getRoomsByStudioAndName().computeIfAbsent(roomKey, key -> {
            log.info("Creating new room: {} for studio: {}", dto.getRoomName(), newStudio.getStudioId());
            Room room = Room.builder()
                    .name(dto.getRoomName())
                    .studio(newStudio)
                    .build();
            return roomRepository.save(room);
        });

        Discipline newDiscipline = cache.getDisciplinesByName().computeIfAbsent(dto.getDisciplineName(), name -> {
            log.info("Creating new discipline for name: {}", name);
            Discipline discipline = Discipline.builder()
                    .name(name)
                    .build();
            return disciplineRepository.save(discipline);
        });

        Instructor newInstructor = cache.getInstructorsByName().computeIfAbsent(dto.getInstructorName(), name -> {
            log.info("Creating new instructor for name: {}", name);
            Instructor instructor = Instructor.builder()
                    .name(name)
                    .build();
            return instructorRepository.save(instructor);
        });

        return Reservation.builder()
                .reservationId(dto.getReservationId())
                .classId(dto.getClassId())
                .room(newRoom)
                .discipline(newDiscipline)
                .instructor(newInstructor)
                .client(newClient)
                .reservationDate(dto.getDay())
                .reservationTime(dto.getTime())
                .orderCreator(dto.getOrderCreator())
                .paymentMethod(dto.getPaymentMethod())
                .status(dto.getStatus())
                .build();
    }
}
