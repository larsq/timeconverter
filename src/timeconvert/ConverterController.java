package timeconvert;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConverterController {
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");
    private static final Pattern DATE_PATTERN =  Pattern.compile("(\\d{4}-\\d{2}-\\d{2})(T\\d{2}:\\d{2}:\\d{2}(\\.\\d{3})?)?");

    @FXML
    private Button now;

    @FXML
    private RadioButton currentTZ;

    @FXML
    private RadioButton utcTZ;

    @FXML
    private TextField timestampText;

    @FXML
    private TextField dateText;

    private ZoneId zoneId;

    public void setNow() {
        timestampText.setText(String.valueOf(System.currentTimeMillis()));
    }

    public void setUTCTz() {
        zoneId = ZoneId.of("UTC");
        updateTimestampFromDate(null, dateText.getText());
    }

    public void setCurrentTz() {
        zoneId = ZoneId.systemDefault();
        updateTimestampFromDate(null, dateText.getText());
    }

    @FXML
    private void initialize() {
        if(currentTZ.isSelected()) {
            setCurrentTz();
        }

        if(utcTZ.isSelected()) {
            setUTCTz();
        }

        dateText.textProperty().addListener((observable, oldValue, newValue) -> {
            updateTimestampFromDate(oldValue, newValue);
        });

        timestampText.textProperty().addListener((observable, oldValue, newValue) -> {
            updateDateFromTimestamp(oldValue, newValue);
        });

        timestampText.setText(String.valueOf(System.currentTimeMillis()));
    }

    private void updateDateFromTimestamp(String oldValue, String newValue) {
        if(Objects.equals(oldValue, newValue)) {
            return;
        }

        if(!NUMBER_PATTERN.matcher(newValue.trim()).matches()) {
            return;
        }

        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(newValue)), zoneId);
        String formatted = zonedDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        dateText.setText(formatted);
    }

    private void updateTimestampFromDate(String oldValue, String newValue) {
        if(Objects.equals(oldValue, newValue)) {
            return;
        }

        Matcher matcher = DATE_PATTERN.matcher(newValue.trim());

        if(!matcher.matches()) {
            return;
        }

        String datePart = matcher.group(1);
        String nanoPart = Optional.ofNullable(matcher.group(3)).orElse(".000");
        String timePart = Optional.ofNullable(matcher.group(2)).orElse("T00:00:00"+ nanoPart);

        ZonedDateTime zonedDateTime = LocalDateTime.parse(datePart + timePart, DateTimeFormatter.ISO_LOCAL_DATE_TIME).atZone(zoneId);
        timestampText.setText(String.valueOf(zonedDateTime.toInstant().toEpochMilli()));
    }
}
