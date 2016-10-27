/**
     * Checks if a menu is available for a given day and time of the week
     * Ex: When requestedDateTime is 05:00AM and menu's ValidTimeFrom is 18:00 and ValidTimeThrough is 07:30, check if 
     * requestedDateTime is within range of the menu's valid times.
     * @param menuEntity Contains Date objects: ValidTimeFrom and ValidTimeThrough
     * @param requestedDateTime If null, then check is done against current UTC time.
     * @param dayOfWeekId The day of the week id, where Sunday is 1, Monday is 2, etc...
     * @return true if the menu is available, false otherwise
     * @throws SQLException
     */
    public boolean isMenuAvailable(MenuEntity menuEntity, Date requestedDateTime, int dayOfWeekId) throws SQLException
    {
        
        if(getSchedule("MenuId", menuEntity.getId(), dayOfWeekId) == null){
            return false;
        }
        
        LocalTime requestedUtcTime = requestedDateTime == null ? new LocalTime(DateTimeZone.UTC) : new DateTime(requestedDateTime, DateTimeZone.UTC).toLocalTime();    
        
        LocalTime startTime = LocalTime.parse(menuEntity.getValidTimeFrom());
        LocalTime endTime = LocalTime.parse(menuEntity.getValidTimeThrough());
        
        if(startTime.isAfter(endTime))
        {
            if(requestedUtcTime.isAfter(endTime) && requestedUtcTime.isBefore(startTime))
                return false;
        }
        else
        {
            if(requestedUtcTime.isBefore(startTime) || requestedUtcTime.isAfter(endTime))
                return false;
        }
        
        return true;
    }