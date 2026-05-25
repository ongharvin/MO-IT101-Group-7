package motorphfinalcodems2;

import java.io.*;
import java.text.*;
import java.util.*;

public class Motorphfinalcodems2 {

    public static HashMap<String, String[]> employeeMap = new HashMap<>();
    public static Map<String, Map<String, String[]>> attendanceMap = new HashMap<>();

    public static void loadEmployeeData() {
        String csvFile = System.getProperty("user.dir")
                + "/terminalassessment/motorph_employee_data_complete.csv";

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
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
                }
            }

        } catch (IOException e) {
            System.out.println("Employee CSV not found: " + csvFile);
        }
    }

    public static void loadAttendanceData() {
        String csvFile = System.getProperty("user.dir")
                + "/terminalassessment/attendance_record.csv";

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
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
                    attendanceMap.get(empNumber).put(date,
                            new String[]{firstName, lastName, logIn, logOut});
                }
            }

        } catch (IOException e) {
            System.out.println("Attendance CSV not found: " + csvFile);
        }
    }

    public static String displayEmployeeInfo(String empNumber) {
        if (employeeMap.containsKey(empNumber)) {
            String[] details = employeeMap.get(empNumber);

            return "Employee Number: " + empNumber
                    + "\nFull Name: " + details[0]
                    + "\nBirthday: " + details[1]
                    + "\nBasic Salary: " + details[2]
                    + "\nHourly Rate: " + details[3];
        }

        return "Employee not found.";
    }

    public static String computeHoursWorked(String empNumber, String startDateStr, String endDateStr) {
        if (!attendanceMap.containsKey(empNumber)) {
            return "Employee not found in attendance records.";
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        StringBuilder result = new StringBuilder();

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
                    long minutes = calculateTimeDifferenceMinutes(data[2], data[3]);
                    totalMinutes += minutes;

                    result.append("Date: ")
                            .append(dateStr)
                            .append(" | Hours Worked: ")
                            .append(formatTimeDifference(minutes))
                            .append("\n");
                }
            }

            result.append("\nTotal Hours Worked: ")
                    .append(formatTimeDifference(totalMinutes));

            return result.toString();

        } catch (ParseException e) {
            return "Invalid date format. Use MM/dd/yyyy.";
        }
    }

    public static String computeGrossSalary(String empNumber, String startDateStr, String endDateStr) {
        if (!employeeMap.containsKey(empNumber)) {
            return "Employee not found.";
        }

        if (!attendanceMap.containsKey(empNumber)) {
            return "Employee not found in attendance records.";
        }

        String[] empDetails = employeeMap.get(empNumber);

        double hourlyRate;

        try {
            hourlyRate = Double.parseDouble(
                    empDetails[3].replaceAll("\"", "").replaceAll(",", "")
            );
        } catch (NumberFormatException e) {
            return "Invalid hourly rate.";
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

        try {
            Date startDate = dateFormat.parse(startDateStr);
            Date endDate = dateFormat.parse(endDateStr);

            long totalMinutes = 0;
            Map<String, String[]> attendanceData = attendanceMap.get(empNumber);

            for (Map.Entry<String, String[]> entry : attendanceData.entrySet()) {
                Date currentDate = dateFormat.parse(entry.getKey());

                if (!currentDate.before(startDate) && !currentDate.after(endDate)) {
                    String[] data = entry.getValue();
                    totalMinutes += calculateTimeDifferenceMinutes(data[2], data[3]);
                }
            }

            double totalHours = totalMinutes / 60.0;
            double grossSalary = totalHours * hourlyRate;

            return "Gross Salary Computation"
                    + "\nEmployee: " + empDetails[0]
                    + "\nEmployee Number: " + empNumber
                    + "\nTotal Hours: " + String.format("%.2f", totalHours)
                    + "\nHourly Rate: PHP " + String.format("%.2f", hourlyRate)
                    + "\nGross Salary: PHP " + String.format("%.2f", grossSalary);

        } catch (ParseException e) {
            return "Invalid date format. Use MM/dd/yyyy.";
        }
    }

    public static String computeNetSalary(String empNumber) {
        if (!employeeMap.containsKey(empNumber)) {
            return "Employee not found.";
        }

        String[] empDetails = employeeMap.get(empNumber);

        double basicSalary;

        try {
            basicSalary = Double.parseDouble(
                    empDetails[2].replaceAll("\"", "").replaceAll(",", "")
            );
        } catch (NumberFormatException e) {
            return "Invalid basic salary.";
        }

        double sss = calculateSSSContribution(basicSalary);
        double philhealth = calculatePhilHealthContribution(basicSalary);
        double pagibig = calculatePagIBIGContribution(basicSalary);

        double taxableIncome = basicSalary - (sss + philhealth + pagibig);
        double withholdingTax = calculateWithholdingTax(taxableIncome);
        double netSalary = taxableIncome - withholdingTax;

        return "Net Salary Computation"
                + "\nEmployee: " + empDetails[0]
                + "\nEmployee Number: " + empNumber
                + "\nBasic Salary: PHP " + String.format("%.2f", basicSalary)
                + "\nSSS: PHP " + String.format("%.2f", sss)
                + "\nPhilHealth: PHP " + String.format("%.2f", philhealth)
                + "\nPag-IBIG: PHP " + String.format("%.2f", pagibig)
                + "\nTaxable Income: PHP " + String.format("%.2f", taxableIncome)
                + "\nWithholding Tax: PHP " + String.format("%.2f", withholdingTax)
                + "\nNet Salary: PHP " + String.format("%.2f", netSalary);
    }

    private static double calculateSSSContribution(double basicSalary) {
        if (basicSalary < 3250) return 135.0;
        if (basicSalary >= 24750) return 1125.0;

        double salaryAboveMin = basicSalary - 3250;
        double tiers = Math.floor(salaryAboveMin / 500);
        return 135.0 + ((tiers + 1) * 22.50);
    }

    private static double calculatePhilHealthContribution(double basicSalary) {
        if (basicSalary <= 10000) return 150.0;
        if (basicSalary < 60000) return basicSalary * 0.015;
        return 900.0;
    }

    private static double calculatePagIBIGContribution(double basicSalary) {
        if (basicSalary >= 1000 && basicSalary <= 1500) return basicSalary * 0.01;
        if (basicSalary > 1500) return basicSalary * 0.02;
        return 0.0;
    }

    private static double calculateWithholdingTax(double taxableIncome) {
        if (taxableIncome <= 20832) return 0.0;
        if (taxableIncome <= 33333) return (taxableIncome - 20833) * 0.20;
        if (taxableIncome <= 66667) return 2500 + ((taxableIncome - 33333) * 0.25);
        if (taxableIncome <= 166667) return 10833 + ((taxableIncome - 66667) * 0.30);
        if (taxableIncome <= 666667) return 40833.33 + ((taxableIncome - 166667) * 0.32);
        return 200833.33 + ((taxableIncome - 666667) * 0.35);
    }

    private static long calculateTimeDifferenceMinutes(String logIn, String logOut) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");

        try {
            Date timeIn = format.parse(logIn);
            Date timeOut = format.parse(logOut);
            return (timeOut.getTime() - timeIn.getTime()) / (60 * 1000);
        } catch (ParseException e) {
            return 0;
        }
    }

    private static String formatTimeDifference(long minutes) {
        return String.format("%d:%02d", minutes / 60, minutes % 60);
    }
}
