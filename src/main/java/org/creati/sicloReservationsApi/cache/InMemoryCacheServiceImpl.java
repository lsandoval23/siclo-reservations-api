package org.creati.sicloReservationsApi.cache;

import org.creati.sicloReservationsApi.cache.model.EntityCache;
import org.creati.sicloReservationsApi.dao.postgre.ClientRepository;
import org.creati.sicloReservationsApi.dao.postgre.DisciplineRepository;
import org.creati.sicloReservationsApi.dao.postgre.InstructorRepository;
import org.creati.sicloReservationsApi.dao.postgre.ReservationRepository;
import org.creati.sicloReservationsApi.dao.postgre.RoomRepository;
import org.creati.sicloReservationsApi.dao.postgre.StudioRepository;
import org.creati.sicloReservationsApi.dao.postgre.model.Reservation;
import org.creati.sicloReservationsApi.file.model.PaymentDto;
import org.creati.sicloReservationsApi.file.model.ReservationDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class InMemoryCacheServiceImpl implements EntityCacheService {

    private final ClientRepository clientRepository;
    private final StudioRepository studioRepository;
    private final DisciplineRepository disciplineRepository;
    private final InstructorRepository instructorRepository;
    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;

    public InMemoryCacheServiceImpl(ClientRepository clientRepository, StudioRepository studioRepository, DisciplineRepository disciplineRepository, InstructorRepository instructorRepository, RoomRepository roomRepository, ReservationRepository reservationRepository) {
        this.clientRepository = clientRepository;
        this.studioRepository = studioRepository;
        this.disciplineRepository = disciplineRepository;
        this.instructorRepository = instructorRepository;
        this.roomRepository = roomRepository;
        this.reservationRepository = reservationRepository;
    }

    @Override
    public EntityCache preloadEntitiesForReservation(List<ReservationDto> reservations) {

        EntityCache entityCache = new EntityCache();

        // Preload clients by email
        Set<String> clientEmails = reservations.stream()
                .map(ReservationDto::getClientEmail)
                .collect(Collectors.toSet());

        clientRepository.findAll().forEach(client -> {
            if (clientEmails.contains(client.getEmail())) {
                entityCache.getClientsByEmail().put(client.getEmail(), client);
            }
        });

        // Preload studios
        Set<String> studioNames = reservations.stream()
                .map(ReservationDto::getStudioName)
                .collect(Collectors.toSet());

        studioRepository.findAll().forEach(studio -> {
            if (studioNames.contains(studio.getName())) {
                entityCache.getStudiosByName().put(studio.getName(), studio);
            }
        });

        // Preload disciplines
        Set<String> disciplineNames = reservations.stream()
                .map(ReservationDto::getDisciplineName)
                .collect(Collectors.toSet());
        disciplineRepository.findAll().forEach(discipline -> {
            if (disciplineNames.contains(discipline.getName())) {
                entityCache.getDisciplinesByName().put(discipline.getName(), discipline);
            }
        });

        // Preload instructors
        Set<String> instructorNames = reservations.stream()
                .map(ReservationDto::getInstructorName)
                .collect(Collectors.toSet());
        instructorRepository.findAll().forEach(instructor -> {
            if (instructorNames.contains(instructor.getName())) {
                entityCache.getInstructorsByName().put(instructor.getName(), instructor);
            }
        });

        // Preload rooms
        roomRepository.findAll().forEach(room -> {
            String key = room.getStudio().getName() + "|" + room.getName();
            entityCache.getRoomsByStudioAndName().put(key, room);
        });

        // Pre-cargar IDs de reservas existentes
        Set<Long> reservationIds = reservations.stream()
                .map(ReservationDto::getReservationId)
                .collect(Collectors.toSet());
        entityCache.getExistingReservationIds().addAll(reservationRepository.findAllById(reservationIds)
                .stream()
                .map(Reservation::getReservationId)
                .collect(Collectors.toSet()));

        return entityCache;
    }

    @Override
    public EntityCache preloadEntitiesForPayments(List<PaymentDto> payments) {

        EntityCache entityCache = new EntityCache();

        // Preload clients by email
        Set<String> clientEmails = payments.stream()
                .map(PaymentDto::getClientEmail)
                .collect(Collectors.toSet());

        clientRepository.findAll().forEach(client -> {
            if (clientEmails.contains(client.getEmail())) {
                entityCache.getClientsByEmail().put(client.getEmail(), client);
            }
        });

        return entityCache;
    }
}
