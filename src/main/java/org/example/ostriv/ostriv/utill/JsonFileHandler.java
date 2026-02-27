package org.example.ostriv.ostriv.utill;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class JsonFileHandler {
    /**
     * Читає вміст JSON файлу та повертає його як рядок.
     * 
     * @param filePath шлях до JSON файлу
     * @return вміст файлу як рядок
     * @throws IOException якщо виникла помилка при читанні файлу
     */
    public String readFile(String filePath) throws IOException {
        try {
            return new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IOException("Помилка читання файлу: " + filePath, e);
        }
    }

    /**
     * Записує рядок у JSON файл.
     * 
     * @param filePath шлях до JSON файлу
     * @param content  вміст для запису
     * @throws IOException якщо виникла помилка при записі файлу
     */
    public void writeFile(String filePath, String content) throws IOException {
        try {
            // Створюємо директорію, якщо вона не існує
            File file = new File(filePath);
            file.getParentFile().mkdirs();

            // Записуємо вміст у файл
            try (BufferedWriter writer = Files.newBufferedWriter(
                    Paths.get(filePath), StandardCharsets.UTF_8)) {
                writer.write(content);
            }
        } catch (IOException e) {
            throw new IOException("Помилка запису файлу: " + filePath, e);
        }
    }

    /**
     * Перевіряє, чи існує файл за вказаним шляхом.
     * 
     * @param filePath шлях до файлу
     * @return true, якщо файл існує, false - інакше
     */
    public boolean fileExists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }
}
