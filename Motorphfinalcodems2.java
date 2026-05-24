/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package motorphfinalcodems2;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 *
 * @author jonad
 */
public class Motorphfinalcodems2 {

    /**
     * @param args the command line arguments
     */
  private static HashMap<String, String[]> employeeMap = new HashMap<>();
    private static Map<String, Map<String, String[]>> attendanceMap = new HashMap<>();

    public static void main(String[] args) {
        loadEmployeeData();
        loadAttendanceData();
            //setup main menu
        Scanner scanner = new Scanner(System.in);
        int choice;
        do {
            System.out.println("\nWelcome to MotorPH Menu:");
            System.out.println("1. Display Employee Information");
            System.out.println("2. Compute Hours Worked");
            System.out.println("3. Compute Gross Salary");
            System.out.println("4. Compute Net Salary");
            System.out.println("5. Exit");
            System.out.print("Enter your choice: ");
            choice = scanner.nextInt();
            scanner.nextLine();  // Consume newline
               //setup options
            switch (choice) {
                case 1:
                    displayEmployeeInfo(scanner);
                    break;
                case 2:
                    computeHoursWorked(scanner);
                    break;
                case 3:
                    computeGrossSalary(scanner);
                    break;
                case 4:
                    computeNetSalary(scanner);
                    break;
                case 5:
                    System.out.println("Okay, Bye!");
                    break;
                default:
                    System.out.println("Option not in menu");
            }
        } while (choice != 5);
        scanner.close();
    }
      
    private static void loadEmployeeData() {
        //read csv file for employee data
        String csvFile = System.getProperty("user.dir") + "/terminalassessment/motorph_employee_data_complete.csv";;
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            // Skip header and capture needed columns
            br.readLine(); 
            String line;
            while ((line = br.readLine()) != null) {
                String[] row = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                if (row.length >= 19) {
                    String employeeNumber = row[0].trim();
                    String lastName = row[1].trim();
                    String firstName = row[2].trim();
                    String birthday = row[3].trim();
                    String basicSalary = row[13].trim(); 
                    String hourlyRate = row[18].trim(); 
                    employeeMap.put(employeeNumber, new String[]{
                            firstName + " " + lastName,
                            birthday,
                            basicSalary,
                            hourlyRate
                    });
                } else {
                    System.out.println("Skipping invalid row: " + line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
        
    private static void loadAttendanceData() {
        //read csv file for attendance
        String csvFile = System.getProperty("user.dir") + "/terminalassessment/attendance_record.csv";;
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            // Skip header
            br.readLine(); 
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 6) {
                    String empNumber = data[0].trim();
                    String lastName = data[1].trim();
                    String firstName = data[2].trim();
                    String date = data[3].trim();
                    String logIn = data[4].trim();
                    String logOut = data[5].trim();
                    attendanceMap.putIfAbsent(empNumber, new HashMap<>());
                    attendanceMap.get(empNumber).put(date, new String[]{firstName, lastName, logIn, logOut});
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //option 1 display employee information    
    private static void displayEmployeeInfo(Scanner scanner) {
        System.out.print("Enter Employee Number: ");
        String empNumber = scanner.nextLine().trim();
        if (employeeMap.containsKey(empNumber)) {
            String[] details = employeeMap.get(empNumber);
            System.out.println("\nEmployee Details:");
            System.out.println("Employee Number: " + empNumber);
            System.out.println("Full Name: " + details[0]);
            System.out.println("Birthday: " + details[1]);
            System.out.println("Other details:");
            System.out.println("Basic Salary: " + details[2]);
            System.out.println("Hourly Rate: " + details[3]);
        } else {
            System.out.println("Employee not found.");
        }
    }
    //option 2 compute hours worked
    private static void computeHoursWorked(Scanner scanner) {
        System.out.print("Enter employee number: ");
        String empNumber = scanner.nextLine();
        if (!attendanceMap.containsKey(empNumber)) {
            System.out.println("Employee not found in attendance records.");
            return;
        }

        System.out.print("Enter start date (MM/dd/yyyy): ");
        String startDateStr = scanner.nextLine();
        System.out.print("Enter end date (MM/dd/yyyy): ");
        String endDateStr = scanner.nextLine();

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        try {
            Date startDate = dateFormat.parse(startDateStr);
            Date endDate = dateFormat.parse(endDateStr);

            long totalMinutes = 0;
            Map<String, String[]> attendanceData = attendanceMap.get(empNumber);
            for (Map.Entry<String, String[]> entry : attendanceData.entrySet()) {
                String dateStr = entry.getKey();
                Date currentDate = dateFormat.parse(dateStr);
                if (!currentDate.before(startDate) && !currentDate.after(endDate)) {
                    String[] data = entry.getValue();
                    String logIn = data[2];
                    String logOut = data[3];
                    long minutes = calculateTimeDifferenceMinutes(logIn, logOut);
                    totalMinutes += minutes;
                    String formatted = formatTimeDifference(minutes);
                    System.out.println("Date: " + dateStr + ", Hours worked: " + formatted);
                }
            }

            String totalFormatted = formatTimeDifference(totalMinutes);
            System.out.println("\nTotal hours worked between " + startDateStr + " and " + endDateStr + ": " + totalFormatted);

        } catch (ParseException e) {
            System.out.println("Invalid date format.");
        }
    }
    //option 3 compute gross salary
    private static void computeGrossSalary(Scanner scanner) {
        System.out.print("Enter employee number: ");
        String empNumber = scanner.nextLine();
        if (!attendanceMap.containsKey(empNumber)) {
            System.out.println("Employee not found in attendance records.");
            return;
        }
        if (!employeeMap.containsKey(empNumber)) {
            System.out.println("Employee not found in employee data.");
            return;
        }

        String[] empDetails = employeeMap.get(empNumber);
        String hourlyRateStr = empDetails[3];
        double hourlyRate;
        try {
            hourlyRate = Double.parseDouble(hourlyRateStr);
        } catch (NumberFormatException e) {
            System.out.println("Invalid hourly rate format for employee: " + hourlyRateStr);
            return;
        }

        System.out.print("Enter start date (MM/dd/yyyy): ");
        String startDateStr = scanner.nextLine();
        System.out.print("Enter end date (MM/dd/yyyy): ");
        String endDateStr = scanner.nextLine();

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        try {
            Date startDate = dateFormat.parse(startDateStr);
            Date endDate = dateFormat.parse(endDateStr);

            long totalMinutes = 0;
            Map<String, String[]> attendanceData = attendanceMap.get(empNumber);
            for (Map.Entry<String, String[]> entry : attendanceData.entrySet()) {
                String dateStr = entry.getKey();
                Date currentDate = dateFormat.parse(dateStr);
                if (!currentDate.before(startDate) && !currentDate.after(endDate)) {
                    String[] data = entry.getValue();
                    String logIn = data[2];
                    String logOut = data[3];
                    long minutes = calculateTimeDifferenceMinutes(logIn, logOut);
                    totalMinutes += minutes;
                }
            }

            double totalHours = totalMinutes / 60.0;
            double grossSalary = totalHours * hourlyRate;
            System.out.printf("\nGross salary for %s (%s) between %s and %s: PHP %.2f%n",
                    empDetails[0], empNumber, startDateStr, endDateStr, grossSalary);

        } catch (ParseException e) {
            System.out.println("Invalid date format.");
        }
    }
    //compute net salary
   private static void computeNetSalary(Scanner scanner) {
    System.out.print("Enter employee number: ");
    String empNumber = scanner.nextLine();
    if (!employeeMap.containsKey(empNumber)) {
        System.out.println("Employee not found.");
        return;
    }

    String[] empDetails = employeeMap.get(empNumber);
    String basicSalaryStr = empDetails[2].replaceAll("^\"|\"$", ""); // Remove quotes
    double basicSalary;
    try {
        basicSalary = Double.parseDouble(basicSalaryStr.replaceAll(",", ""));
    } catch (NumberFormatException e) {
        System.out.println("Invalid basic salary format for employee: " + basicSalaryStr);
        return;
    }


        // Compute contributions
        double sss = calculateSSSContribution(basicSalary);
        double philhealth = calculatePhilHealthContribution(basicSalary);
        double pagibig = calculatePagIBIGContribution(basicSalary);

        // Compute taxable income
        double taxableIncome = basicSalary - (sss + philhealth + pagibig);

        // Compute withholding tax
        double withholdingTax = calculateWithholdingTax(taxableIncome);

        // Compute net salary
        double netSalary = taxableIncome - withholdingTax;

        // Display results
        System.out.println("\nNet Salary Calculation for Employee: " + empDetails[0] + " (" + empNumber + ")");
        System.out.printf("Basic Salary: PHP %.2f%n", basicSalary);
        System.out.printf("SSS Contribution: PHP %.2f%n", sss);
        System.out.printf("PhilHealth Contribution: PHP %.2f%n", philhealth);
        System.out.printf("Pag-IBIG Contribution: PHP %.2f%n", pagibig);
        System.out.printf("Total Deductions: PHP %.2f%n", (sss + philhealth + pagibig));
        System.out.printf("Taxable Income: PHP %.2f%n", taxableIncome);
        System.out.printf("Withholding Tax: PHP %.2f%n", withholdingTax);
        System.out.printf("Net Salary: PHP %.2f%n", netSalary);
    }
   //SSS Contribution
    private static double calculateSSSContribution(double basicSalary) {
    // SSS Contribution Summary:
    // - Below 3,250: Flat rate of 135.00
    // - 3,250 to 24,750: Incremental tiers every 500 pesos
    // - 24,750 and above: Flat rate of 1,125.00

    final double MIN_SALARY = 3250.0;
    final double MAX_SALARY = 24750.0;
    final double BASE_CONTRIBUTION = 135.0;
    final double TIER_STEP = 500.0;
    final double CONTRIBUTION_INCREMENT = 22.50;

    // Handle lowest bracket
    if (basicSalary < MIN_SALARY) {
        return BASE_CONTRIBUTION;
    }
    
    // Handle highest bracket
    if (basicSalary >= MAX_SALARY) {
        return 1125.0;
    }

    // Calculate contribution for middle brackets
    double salaryAboveMin = basicSalary - MIN_SALARY;
    double numberOfCompleteTiers = Math.floor(salaryAboveMin / TIER_STEP);
    double contributionAdjustment = (numberOfCompleteTiers + 1) * CONTRIBUTION_INCREMENT;
    
    return BASE_CONTRIBUTION + contributionAdjustment;
}
    //Philhealth contribution
    private static double calculatePhilHealthContribution(double basicSalary) {
        if (basicSalary <= 10000) {
            return 150.0;
        } else if (basicSalary < 60000) {
            return basicSalary * 0.015;
        } else {
            return 900.0;
        }
    }
    //Pag-ibig contribution
    private static double calculatePagIBIGContribution(double basicSalary) {
        if (basicSalary >= 1000 && basicSalary <= 1500) {
            return basicSalary * 0.01;
        } else if (basicSalary > 1500) {
            return basicSalary * 0.02;
        } else {
            return 0.0;
        }
    }
    //Withholding tax
    private static double calculateWithholdingTax(double taxableIncome) {
        if (taxableIncome <= 20832) {
            return 0.0;
        } else if (taxableIncome <= 33333) {
            double excess = taxableIncome - 20833;
            return excess * 0.20;
        } else if (taxableIncome <= 66667) {
            double excess = taxableIncome - 33333;
            return 2500 + (excess * 0.25);
        } else if (taxableIncome <= 166667) {
            double excess = taxableIncome - 66667;
            return 10833 + (excess * 0.30);
        } else if (taxableIncome <= 666667) {
            double excess = taxableIncome - 166667;
            return 40833.33 + (excess * 0.32);
        } else {
            double excess = taxableIncome - 666667;
            return 200833.33 + (excess * 0.35);
        }
    }

    private static long calculateTimeDifferenceMinutes(String logIn, String logOut) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        try {
            Date timeIn = format.parse(logIn);
            Date timeOut = format.parse(logOut);
            return (timeOut.getTime() - timeIn.getTime()) / (60 * 1000);
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static String formatTimeDifference(long minutes) {
        if (minutes < 0) return "Invalid time";
        return String.format("%d:%02d", minutes / 60, minutes % 60);
    }
}
