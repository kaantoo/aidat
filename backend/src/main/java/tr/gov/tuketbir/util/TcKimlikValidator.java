package tr.gov.tuketbir.util;

/**
 * TC Kimlik numarası doğrulama yardımcı sınıfı.
 * Türk vatandaşlık numarası için algoritmik doğrulama yapar.
 */
public final class TcKimlikValidator {

    private TcKimlikValidator() {
        // Utility class
    }

    /**
     * TC Kimlik numarasının geçerliliğini kontrol eder.
     * 
     * @param tcKimlik TC Kimlik numarası (11 haneli)
     * @return true ise geçerli, false ise geçersiz
     */
    public static boolean isValid(String tcKimlik) {
        if (tcKimlik == null || tcKimlik.length() != 11) {
            return false;
        }

        // Sadece rakamlardan oluşmalı
        if (!tcKimlik.matches("\\d{11}")) {
            return false;
        }

        // İlk hane 0 olamaz
        if (tcKimlik.charAt(0) == '0') {
            return false;
        }

        int[] digits = new int[11];
        for (int i = 0; i < 11; i++) {
            digits[i] = Character.getNumericValue(tcKimlik.charAt(i));
        }

        // Algoritma kontrolü
        // 1, 3, 5, 7, 9. hanelerin toplamının 7 katından
        // 2, 4, 6, 8. hanelerin toplamı çıkarılır, mod 10 alınır
        // Sonuç 10. hane ile eşit olmalı
        int oddSum = digits[0] + digits[2] + digits[4] + digits[6] + digits[8];
        int evenSum = digits[1] + digits[3] + digits[5] + digits[7];

        int tenthDigit = ((oddSum * 7) - evenSum) % 10;
        if (tenthDigit < 0) {
            tenthDigit += 10;
        }

        if (digits[9] != tenthDigit) {
            return false;
        }

        // İlk 10 hanenin toplamının mod 10'u 11. hane ile eşit olmalı
        int totalSum = 0;
        for (int i = 0; i < 10; i++) {
            totalSum += digits[i];
        }

        if (digits[10] != (totalSum % 10)) {
            return false;
        }

        return true;
    }
}
