package android.ptv.com.ptv_android.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class Program {
    private String channelName;
    private Integer channelId;
    private Integer titleId;
    private String titleName;
    private String titleDescription;

    private Integer programId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    private Date startTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    private Date endTime;
    private String thumbUrl;
    private String playUrl;

    private Integer currentTimeSeconds;
    private Integer remainingSeconds;
    private String remainingTime;
    private Program upNext;
    private String startsIn;
    private boolean isFiller;
    private Integer fillerCutSeconds;

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public Integer getChannelId() {
        return channelId;
    }

    public void setChannelId(Integer channelId) {
        this.channelId = channelId;
    }

    public Integer getTitleId() {
        return titleId;
    }

    public void setTitleId(Integer titleId) {
        this.titleId = titleId;
    }

    public String getTitleName() {
        return titleName;
    }

    public void setTitleName(String titleName) {
        this.titleName = titleName;
    }

    public String getTitleDescription() {
        return titleDescription;
    }

    public void setTitleDescription(String titleDescription) {
        this.titleDescription = titleDescription;
    }

    public Integer getProgramId() {
        return programId;
    }

    public void setProgramId(Integer programId) {
        this.programId = programId;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getThumbUrl() {
        return thumbUrl;
    }

    public void setThumbUrl(String thumbUrl) {
        this.thumbUrl = thumbUrl;
    }

    public String getPlayUrl() {
        return playUrl;
    }

    public void setPlayUrl(String playUrl) {
        this.playUrl = playUrl;
    }

    public Integer getCurrentTimeSeconds() {
        return currentTimeSeconds;
    }

    public void setCurrentTimeSeconds(Integer currentTimeSeconds) {
        this.currentTimeSeconds = currentTimeSeconds;
    }

    public Integer getRemainingSeconds() {
        return remainingSeconds;
    }

    public void setRemainingSeconds(Integer remainingSeconds) {
        this.remainingSeconds = remainingSeconds;
    }

    public String getRemainingTime() {
        return remainingTime;
    }

    public void setRemainingTime(String remainingTime) {
        this.remainingTime = remainingTime;
    }

    public Program getUpNext() {
        return upNext;
    }

    public void setUpNext(Program upNext) {
        this.upNext = upNext;
    }

    public String getStartsIn() {
        return startsIn;
    }

    public void setStartsIn(String startsIn) {
        this.startsIn = startsIn;
    }

    public boolean getIsFiller() {
        return isFiller;
    }

    public void setFiller(boolean filler) {
        isFiller = filler;
    }

    public Integer getFillerCutSeconds() {
        return fillerCutSeconds;
    }

    public void setFillerCutSeconds(Integer fillerCutSeconds) {
        this.fillerCutSeconds = fillerCutSeconds;
    }

    public boolean hasFillerCutSeconds() {
        if (fillerCutSeconds != null && fillerCutSeconds > 0) {
            return true;
        }
        return false;
    }

    public boolean hasStarted() {
        if (this.currentTimeSeconds > 0) {
            return true;
        }
        return false;
    }
}
