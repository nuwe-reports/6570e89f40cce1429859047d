package com.example.demo;

import static org.assertj.core.api.Assertions.assertThat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.hibernate.id.IdentifierGenerationException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import com.example.demo.entities.*;
import javax.persistence.PersistenceException;

@DataJpaTest
@AutoConfigureTestDatabase(replace=Replace.NONE)
@TestInstance(Lifecycle.PER_CLASS)
class EntityUnitTest {

	@Autowired
	private TestEntityManager entityManager;
	private Doctor doctorToPersist;
	private Patient patientToPersist;
    private Room roomToPersist;
    private Appointment appointmentToPersist;
    private Appointment appointmentToPersist2;
    private Appointment appointmentToPersist3;

    @BeforeEach
    void beforeEach(){
        this.patientToPersist = new Patient("Jose Luis", "Olaya", 37, "j.olaya@email.com");
        this.doctorToPersist  = new Doctor ("Perla", "Amalia", 24, "p.amalia@hospital.accwe");
        this.roomToPersist    = new Room("Dermatology");
    }

    @Test
    void shouldPersistAppointmentWithAllData() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

        LocalDateTime startsAt= LocalDateTime.parse("19:30 24/04/2023", formatter);
        LocalDateTime finishesAt = LocalDateTime.parse("20:30 24/04/2023", formatter);

        this.appointmentToPersist = new Appointment(this.patientToPersist, this.doctorToPersist, this.roomToPersist, startsAt, finishesAt);

        entityManager.persist(this.patientToPersist);
        entityManager.flush();
        entityManager.persist(this.doctorToPersist);
        entityManager.flush();
        entityManager.persist(this.roomToPersist);
        entityManager.flush();
        entityManager.persist(this.appointmentToPersist);
        entityManager.flush();

        Appointment persistedAppointment = entityManager.find(Appointment.class, this.appointmentToPersist.getId());

        assertThat(persistedAppointment).isNotNull();
        assertThat(persistedAppointment.getPatient()).isEqualTo( this.appointmentToPersist.getPatient());
        assertThat(persistedAppointment.getDoctor()).isEqualTo( this.appointmentToPersist.getDoctor());
        assertThat(persistedAppointment.getRoom()).isEqualTo( this.appointmentToPersist.getRoom());
        assertThat(persistedAppointment.getStartsAt()).isEqualTo( this.appointmentToPersist.getStartsAt());
        assertThat(persistedAppointment.getFinishesAt()).isEqualTo( this.appointmentToPersist.getFinishesAt());
    }

    @Test
    void shouldPersistAppointmentNull() {
        this.appointmentToPersist = new Appointment();

        entityManager.persist(this.appointmentToPersist);
        entityManager.flush();

        Appointment persistedAppointment = entityManager.find(Appointment.class, this.appointmentToPersist.getId());
        assertThat(persistedAppointment).isNotNull();

        assertThat(persistedAppointment.getId()).isEqualTo(this.appointmentToPersist.getId());
        assertThat(persistedAppointment.getPatient()).isNull();
        assertThat(persistedAppointment.getDoctor()).isNull();
        assertThat(persistedAppointment.getRoom()).isNull();
        assertThat(persistedAppointment.getStartsAt()).isNull();
        assertThat(persistedAppointment.getFinishesAt()).isNull();
    }

    @Test
    @DisplayName("method overlaps should return true when startsAt and finishesAt times are the same in the same room")
    void OverlapsReturnsTrueWhenStartAndEndTimesAreSame () {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

        LocalDateTime startsAt= LocalDateTime.parse("20:30 24/04/2023", formatter);
        LocalDateTime finishesAt = LocalDateTime.parse("20:30 24/04/2023", formatter);

        this.appointmentToPersist = new Appointment(this.patientToPersist, this.doctorToPersist, this.roomToPersist, startsAt, finishesAt);

        assertThat(appointmentToPersist.overlaps(this.appointmentToPersist)).isTrue();
    }

    @Test
    @DisplayName("method overlaps should return true when two appointment has the same finishesAt time in the same room")
    void OverlapsReturnsTrueWhenAppointmentWithSameFinishedAt() {

        Patient patient2 = new Patient("Paulino", "Antunez", 37, "p.antunez@email.com");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

        LocalDateTime startsAtAppointment= LocalDateTime.parse("19:30 24/04/2023", formatter);
        LocalDateTime finishesAtAppointment = LocalDateTime.parse("20:00 24/04/2023", formatter);

        LocalDateTime startsAtAppointment1= LocalDateTime.parse("19:45 24/04/2023", formatter);
        LocalDateTime finishesAtAppointment1 = LocalDateTime.parse("20:00 24/04/2023", formatter);

        this.appointmentToPersist  = new Appointment(this.patientToPersist, this.doctorToPersist, this.roomToPersist, startsAtAppointment, finishesAtAppointment);
        this.appointmentToPersist2 = new Appointment(patient2, doctorToPersist, this.roomToPersist, startsAtAppointment1, finishesAtAppointment1);

        assertThat(appointmentToPersist.overlaps(appointmentToPersist2)).isTrue();
    }

    @Test
    @DisplayName("method overlaps should return true when there two appointments with same start and end time in the same room")
    void OverlapsReturnsTrueWhenThereAppointmentWithSameTime () {
        Patient patient1 = new Patient("Paulino", "Antunez", 37, "p.antunez@email.com");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

        LocalDateTime startsAtAppointment= LocalDateTime.parse("19:30 24/04/2023", formatter);
        LocalDateTime finishesAtAppointment = LocalDateTime.parse("20:00 24/04/2023", formatter);

        this.appointmentToPersist  = new Appointment(this.patientToPersist, this.doctorToPersist, this.roomToPersist, startsAtAppointment, finishesAtAppointment);
        this.appointmentToPersist2 = new Appointment(patient1, this.doctorToPersist, this.roomToPersist, startsAtAppointment, finishesAtAppointment);

        assertThat(appointmentToPersist.overlaps(appointmentToPersist2)).isTrue();
    }

    @Test
    @DisplayName("method overlaps should return true when two appointments have the same start time in the same room")
    void OverlapsReturnsTrueWhenAppointmentHaveSameStartsAt() {
        Patient patient1 = new Patient("Paulino", "Antunez", 37, "p.antunez@email.com");
        Doctor doctor1   = new Doctor ("Miren", "Iniesta", 24, "m.iniesta@hospital.accwe");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

        LocalDateTime startsAtAppointment= LocalDateTime.parse("19:30 24/04/2023", formatter);
        LocalDateTime finishesAtAppointment = LocalDateTime.parse("19:45 24/04/2023", formatter);

        LocalDateTime startsAtAppointment1= LocalDateTime.parse("19:30 24/04/2023", formatter);
        LocalDateTime finishesAtAppointment1 = LocalDateTime.parse("20:00 24/04/2023", formatter);

        this.appointmentToPersist = new Appointment(this.patientToPersist, this.doctorToPersist, this.roomToPersist, startsAtAppointment, finishesAtAppointment);
        this.appointmentToPersist2 = new Appointment(patient1, doctor1, this.roomToPersist, startsAtAppointment1, finishesAtAppointment1);

        assertThat(appointmentToPersist.overlaps(appointmentToPersist2)).isTrue();
    }
    @Test
    @DisplayName("method overlaps should return false when there three appointments no overlaps in the same room")
    void OverlapsReturnsFalseWhenThereAppointmentWithDistinctTime () {
        Patient patient1 = new Patient("Paulino", "Antunez", 37, "p.antunez@email.com");
        Patient patient2 = new Patient("Sandra", "Solis", 37, "p.sandra@email.com");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

        LocalDateTime startsAtAppointment= LocalDateTime.parse("19:45 24/04/2023", formatter);
        LocalDateTime finishesAtAppointment = LocalDateTime.parse("20:00 24/04/2023", formatter);

        LocalDateTime startsAtAppointment1= LocalDateTime.parse("20:15 24/04/2023", formatter);
        LocalDateTime finishesAtAppointment1 = LocalDateTime.parse("20:30 24/04/2023", formatter);

        LocalDateTime startsAtAppointment2= LocalDateTime.parse("20:45 24/04/2023", formatter);
        LocalDateTime finishesAtAppointment2 = LocalDateTime.parse("21:00 24/04/2023", formatter);

        this.appointmentToPersist  = new Appointment(this.patientToPersist, this.doctorToPersist, this.roomToPersist, startsAtAppointment, finishesAtAppointment);
        this.appointmentToPersist2 = new Appointment(patient1, this.doctorToPersist, this.roomToPersist, startsAtAppointment1, finishesAtAppointment1);
        this.appointmentToPersist3 = new Appointment(patient2, this.doctorToPersist, this.roomToPersist, startsAtAppointment2, finishesAtAppointment2);

        assertThat(appointmentToPersist.overlaps(appointmentToPersist2)).isFalse();
        assertThat(appointmentToPersist.overlaps(appointmentToPersist3)).isFalse();

        assertThat(appointmentToPersist2.overlaps(appointmentToPersist)).isFalse();
        assertThat(appointmentToPersist2.overlaps(appointmentToPersist3)).isFalse();

        assertThat(appointmentToPersist3.overlaps(appointmentToPersist)).isFalse();
        assertThat(appointmentToPersist3.overlaps(appointmentToPersist2)).isFalse();
    }

    @Test
    void shouldGettersAndSettersAppointmentWorkCorrectly() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

        LocalDateTime startsAt= LocalDateTime.parse("19:30 24/04/2023", formatter);
        LocalDateTime finishesAt = LocalDateTime.parse("20:30 24/04/2023", formatter);

        this.appointmentToPersist = new Appointment(this.patientToPersist, this.doctorToPersist, this.roomToPersist, startsAt, finishesAt);
        this.appointmentToPersist.setDoctor(new Doctor ("Miren", "Iniesta", 24, "m.iniesta@hospital.accwe"));

        entityManager.persist(this.patientToPersist);
        entityManager.flush();
        entityManager.persist(this.doctorToPersist);
        entityManager.flush();
        entityManager.persist(this.roomToPersist);
        entityManager.flush();
        entityManager.persist(this.appointmentToPersist);
        entityManager.flush();

        Appointment persistedAppointment = entityManager.find(Appointment.class, this.appointmentToPersist.getId());
        assertThat(persistedAppointment).isNotNull();

        assertThat(persistedAppointment.getPatient()).isEqualTo(this.appointmentToPersist.getPatient());
        assertThat(persistedAppointment.getDoctor()).isEqualTo(this.appointmentToPersist.getDoctor());
        assertThat(persistedAppointment.getRoom()).isEqualTo(this.appointmentToPersist.getRoom());
        assertThat(persistedAppointment.getStartsAt()).isEqualTo(this.appointmentToPersist.getStartsAt());
        assertThat(persistedAppointment.getFinishesAt()).isEqualTo(this.appointmentToPersist.getFinishesAt());
    }

    @Test
    void shouldPersistDoctorWithAllData() {
        this.doctorToPersist = new Doctor ("Perla", "Amalia", 24, "p.amalia@hospital.accwe");

        entityManager.persist(this.doctorToPersist);
        entityManager.flush();

        Doctor persistedDoctor = entityManager.find(Doctor.class, this.doctorToPersist.getId());
        assertThat(persistedDoctor).isNotNull();
        assertThat(persistedDoctor.getFirstName()).isEqualTo("Perla");
        assertThat(persistedDoctor.getLastName()).isEqualTo("Amalia");
        assertThat(persistedDoctor.getAge()).isEqualTo(24);
        assertThat(persistedDoctor.getEmail()).isEqualTo("p.amalia@hospital.accwe");
    }
    @Test
    void shouldPersistDoctorNull() {
        this.doctorToPersist = new Doctor();

        entityManager.persist(this.doctorToPersist);
        entityManager.flush();

        Doctor persistedDoctor = entityManager.find(Doctor.class, this.doctorToPersist.getId());
        assertThat(persistedDoctor).isNotNull();

        assertThat(persistedDoctor.getId()).isEqualTo(this.doctorToPersist.getId());
        assertThat(persistedDoctor.getLastName()).isNull();
        assertThat(persistedDoctor.getLastName()).isNull();
        assertThat(persistedDoctor.getAge()).isEqualTo(0);
        assertThat(persistedDoctor.getEmail()).isNull();
    }

    @Test
     void shouldGettersAndSettersDoctorWorkCorrectly() {
        this.doctorToPersist = new Doctor ("Perla", "Amalia", 24, "p.amalia@hospital.accwe");

        this.doctorToPersist.setAge(25);

        entityManager.persist(this.doctorToPersist);
        entityManager.flush();

        Doctor persistedDoctor = entityManager.find(Doctor.class, this.doctorToPersist.getId());
        assertThat(persistedDoctor).isNotNull();

        assertThat(persistedDoctor.getFirstName()).isEqualTo("Perla");
        assertThat(persistedDoctor.getLastName()).isEqualTo("Amalia");
        assertThat(persistedDoctor.getAge()).isEqualTo(25);
        assertThat(persistedDoctor.getEmail()).isEqualTo("p.amalia@hospital.accwe");
    }

    @Test
     void shouldPersistPatientWithAllData() {
         this.patientToPersist = new Patient("Jose Luis", "Olaya", 37, "j.olaya@email.com");
         entityManager.persist(this.patientToPersist);
         entityManager.flush();

         Patient persistedPatient = entityManager.find(Patient.class, this.patientToPersist.getId());
         assertThat(persistedPatient).isNotNull();
         assertThat(persistedPatient.getFirstName()).isEqualTo(this.patientToPersist.getFirstName());
         assertThat(persistedPatient.getLastName()).isEqualTo(this.patientToPersist.getLastName());
         assertThat(persistedPatient.getAge()).isEqualTo(this.patientToPersist.getAge());
         assertThat(persistedPatient.getEmail()).isEqualTo(this.patientToPersist.getEmail());
    }

    @Test
    void shouldPersistPatientNull() {
        this.patientToPersist = new Patient();

        entityManager.persist(this.patientToPersist);
        entityManager.flush();

        Patient persistedPatient = entityManager.find(Patient.class, this.patientToPersist.getId());
        assertThat(persistedPatient).isNotNull();

        assertThat(persistedPatient.getId()).isEqualTo(this.patientToPersist.getId());
        assertThat(persistedPatient.getLastName()).isNull();
        assertThat(persistedPatient.getLastName()).isNull();
        assertThat(persistedPatient.getAge()).isEqualTo(0);
        assertThat(persistedPatient.getEmail()).isNull();
    }

    @Test
    void shouldGettersAndSettersPatientWorkCorrectly(){
        this.patientToPersist = new Patient("Jose Luis", "Olaya", 37, "j.olaya@email.com");

        this.patientToPersist.setAge(38);

        entityManager.persist(this.patientToPersist);
        entityManager.flush();

        Patient persistedPatient = entityManager.find(Patient.class, this.patientToPersist.getId());
        assertThat(persistedPatient).isNotNull();

        assertThat(persistedPatient.getFirstName()).isEqualTo(this.patientToPersist.getFirstName());
        assertThat(persistedPatient.getLastName()).isEqualTo(this.patientToPersist.getLastName());
        assertThat(persistedPatient.getAge()).isEqualTo(this.patientToPersist.getAge());
        assertThat(persistedPatient.getEmail()).isEqualTo(this.patientToPersist.getEmail());
    }

    @Test
    void shouldPersistRoomWithAllData() {
        this.roomToPersist = new Room("Gynecology");
        entityManager.persist(this.roomToPersist);
        entityManager.flush();

        Room persistedRoom = entityManager.find(Room.class, this.roomToPersist.getRoomName());
        assertThat(persistedRoom).isNotNull();
        assertThat(persistedRoom.getRoomName()).isEqualTo(this.roomToPersist.getRoomName());
    }

    @Test
    void shouldThrowExceptionWhenPersistingEmptyRoom() {
        Room roomToPersist = new Room();
        PersistenceException persistenceException =  null;
        try {
            entityManager.persist(roomToPersist);
            entityManager.flush();
        } catch (PersistenceException thrownException) {
            persistenceException = thrownException;
        }
        assertThat(persistenceException).isNotNull();
        assertThat(persistenceException.getCause()).isInstanceOf(IdentifierGenerationException.class);
    }
}
