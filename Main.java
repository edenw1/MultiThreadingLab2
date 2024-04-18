import javax.swing.JFileChooser;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            runSimulation(selectedFile);
        } else {
            System.out.println("No file selected.");
        }
    }
//package issue?
    private static void runSimulation(File file) {
        MultiThreadingLab2.EquipmentStorage equipmentStorage = new MultiThreadingLab2.EquipmentStorage();
        MultiThreadingLab2.WaitingRoom waitingRoom = new MultiThreadingLab2.WaitingRoom();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            int numEquipment = Integer.parseInt(reader.readLine().trim()); //read in the number of equipment types
            for (int i = 0; i < numEquipment; i++) {
                String[] parts = reader.readLine().trim().split(" ");
                equipmentStorage.addEquipment(parts[0], Integer.parseInt(parts[1])); 
                //add each equipment type
            }

            int numPatients = Integer.parseInt(reader.readLine().trim());
            for (int i = 0; i < numPatients; i++) {
                int treatmentTime = Integer.parseInt(reader.readLine().trim()); // treatment time
                String[] equipmentNeeded = reader.readLine().trim().split(" "); // equipment needed by the patient
                waitingRoom.addPatient(new MultiThreadingLab2.Patient(treatmentTime, Arrays.asList(equipmentNeeded)));
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        int[] docs = {1, 2, 4, 8}; 
        //num doctors
        for (int doctorIndex = 0; doctorIndex < docs.length; doctorIndex++) {
            int numDocs = docs[doctorIndex];
            List<Thread> doctorThreads = new ArrayList<>();
            for (int i = 0; i < numDocs; i++) {
            	MultiThreadingLab2.Doctor doctor = new MultiThreadingLab2.Doctor(waitingRoom, equipmentStorage);
            	Thread thread = new Thread(doctor);
                thread.start();
                doctorThreads.add(thread);
            }

            for (int threadIndex = 0; threadIndex < doctorThreads.size(); threadIndex++) {
                Thread thread = doctorThreads.get(threadIndex);
                try {
                	//note: has to be in a try-catch
                    thread.join();  // use to wait for all doctor threads to complete
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Complete with " + numDocs + " doctors.");
        }
    }
}
