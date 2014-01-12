/*
This file is part of the GhostDriver by Ivan De Marino <http://ivandemarino.me>.

Copyright (c) 2014, Ivan De Marino <http://ivandemarino.me>
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice,
      this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice,
      this list of conditions and the following disclaimer in the documentation
      and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package ghostdriver;

import java.io.File;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FileUploadTest extends BaseTest {
    private static final String LOREM_IPSUM_TEXT = "lorem ipsum dolor sit amet";
    private static final String FILE_HTML = "<div>" + LOREM_IPSUM_TEXT + "</div>";

    @Test
    public void checkFileUploadCompletes() throws IOException {
        WebDriver d = getDriver();

        // Create the test file for uploading
        File testFile = File.createTempFile("webdriver", "tmp");
        testFile.deleteOnExit();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
            new FileOutputStream(testFile.getAbsolutePath()), "utf-8"));
        writer.write(FILE_HTML);
        writer.close();

        // Upload the temp file
        d.get("http://localhost:2310/common/upload.html");
        d.findElement(By.id("upload")).sendKeys(testFile.getAbsolutePath());
        d.findElement(By.id("go")).submit();

        // Uploading files across a network may take a while, even if they're really small.
        // Wait for the loading label to disappear.
        WebDriverWait wait = new WebDriverWait(d, 10);
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("upload_label")));

        d.switchTo().frame("upload_target");

        wait = new WebDriverWait(d, 5);
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.xpath("//body"), LOREM_IPSUM_TEXT));

        // Navigate after file upload to verify callbacks are properly released.
        d.get("http://www.google.com/");
    }

    @Test
    public void checkFileUploadFailsIfFileDoesNotExist() throws InterruptedException {
        WebDriver d = getDriver();

        // Trying to upload a file that doesn't exist
        d.get("http://localhost:2310/common/upload.html");
        d.findElement(By.id("upload")).sendKeys("file_that_does_not_exist.fake");
        d.findElement(By.id("go")).submit();

        // Uploading files across a network may take a while, even if they're really small.
        // Wait for a while and make sure the "upload_label" is still there: means that the file was not uploaded
        Thread.sleep(1000);
        assertTrue(d.findElement(By.id("upload_label")).isDisplayed());
    }

    @Test
    public void checkUploadingTheSameFileMultipleTimes() throws IOException {
        WebDriver d = getDriver();

        File file = File.createTempFile("test", "txt");
        file.deleteOnExit();

        d.get("http://localhost:2310/common/formPage.html");
        WebElement uploadElement = d.findElement(By.id("upload"));
        uploadElement.sendKeys(file.getAbsolutePath());
        uploadElement.submit();

        d.get("http://localhost:2310/common/formPage.html");
        uploadElement = d.findElement(By.id("upload"));
        uploadElement.sendKeys(file.getAbsolutePath());
        uploadElement.submit();
     }

    @Test
    public void checkOnChangeEventIsFiredOnFileUpload() throws IOException {
        WebDriver d = getDriver();

        d.get("http://localhost:2310/common/formPage.html");
        WebElement uploadElement = d.findElement(By.id("upload"));
        WebElement result = d.findElement(By.id("fileResults"));
        assertEquals("", result.getText());

        File file = File.createTempFile("test", "txt");
        file.deleteOnExit();

        uploadElement.sendKeys(file.getAbsolutePath());
        // Shift focus to something else because send key doesn't make the focus leave
        d.findElement(By.id("id-name1")).click();

        assertEquals("changed", result.getText());
     }
}
