import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Semaphore;

public class MultiThreadingLab2 {
	
    // managing available equipment
    static class EquipmentStorage {
        Map<String, Semaphore> equipment = new HashMap<>();
        private final Object lock = new Object(); //object used for synchronization

        // this is where we add equipment to the storage
        public void addEquipment(String name, int quantity) {
            synchronized (lock) {
                equipment.put(name, new Semaphore(quantity));
            }
            System.out.println("Equipment added: " + quantity + " " + name);
        }

        // method to USE equipment
        public boolean useEquipment(List<String> neededEquipment) throws InterruptedException {
            synchronized (lock) {
                for (int i = 0; i < neededEquipment.size(); i++) {
                    String equipmentName = neededEquipment.get(i);
                    Semaphore semaphore = equipment.get(equipmentName);
                    if (semaphore != null) {
                        semaphore.acquire();
                    }
                }
            }
            return true;
        }

        // method to release equipment
        public void releaseEquipment(List<String> usedEquipment) {
            synchronized (lock) {
                for (int i = 0; i < usedEquipment.size(); i++) {
                    String equipmentName = usedEquipment.get(i);
                    Semaphore semaphore = equipment.get(equipmentName);
                    if (semaphore != null) {
                        semaphore.release();
                    }
                }
            }
        }
    }

    static class Patient {
        int treatmentTime;
        List<String> equipmentNeeded;

        //constructor
        public Patient(int treatmentTime, List<String> equipmentNeeded) {
            this.treatmentTime = treatmentTime;
            this.equipmentNeeded = equipmentNeeded;
            String equipmentString = String.join(", ", equipmentNeeded);
            System.out.println("Added patient with treatment time: " + treatmentTime);
            System.out.println("  |  Equipment needed: " + equipmentString);
        }
    }

    // this is where patients will "wait" for treatment
    static class WaitingRoom {
        Queue<Patient> patients = new LinkedList<>();

        //method to get the next patient
        public synchronized Patient getNextPatient() {
        	//ask if this is wise to use/beneficial
            return patients.poll();
        }

        // call to add a patient to the waiting room
        public void addPatient(Patient patient) {
            patients.add(patient);
        }
    }

    // used to represent a doctor who treats patients
    static class Doctor implements Runnable {
        private WaitingRoom waitingRoom;
        private EquipmentStorage equipmentStorage;

        // constructor
        public Doctor(WaitingRoom waitingRoom, EquipmentStorage equipmentStorage) {
            this.waitingRoom = waitingRoom;
            this.equipmentStorage = equipmentStorage;
        }

        @Override
        public void run() {
            try {
                Patient patient;
                while ((patient = waitingRoom.getNextPatient()) != null) {
                    System.out.println("Doctor started treating a patient that needs " + patient.treatmentTime);
                    if (equipmentStorage.useEquipment(patient.equipmentNeeded)) {
                        Thread.sleep(patient.treatmentTime); // Sleep for treatment time
                        equipmentStorage.releaseEquipment(patient.equipmentNeeded); // Release used equipment
                        System.out.println("Doctor has finished.");
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
