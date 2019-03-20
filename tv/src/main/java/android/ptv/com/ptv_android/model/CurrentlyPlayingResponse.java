package android.ptv.com.ptv_android.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;
import java.util.List;

public class CurrentlyPlayingResponse extends APIResponse {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    private Date currentTime;
    private Integer currentTimeUnix;
    private List<Program> programs;

    public Date getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(Date currentTime) {
        this.currentTime = currentTime;
    }

    public Integer getCurrentTimeUnix() {
        return currentTimeUnix;
    }

    public void setCurrentTimeUnix(Integer currentTimeUnix) {
        this.currentTimeUnix = currentTimeUnix;
    }

    public List<Program> getPrograms() {
        return programs;
    }

    public void setPrograms(List<Program> programs) {
        this.programs = programs;
    }
}
