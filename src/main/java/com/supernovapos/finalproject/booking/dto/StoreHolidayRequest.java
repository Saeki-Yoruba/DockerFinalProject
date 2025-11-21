package com.supernovapos.finalproject.booking.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.supernovapos.finalproject.booking.model.StoreHolidays;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StoreHolidayRequest {
	

   @NotNull(message = "holidayDate 不能為空")
   @JsonFormat(pattern = "yyyy-MM-dd")
   private LocalDate holidayDate;

   /**  
    * 是否每年重複 (預設 false)  
    */
   private Boolean isRecurring = false;

   /**  
    * 公休原因 (最多 200 字)  
    */
   @Size(max = 200, message = "reason 最多 200 個字元")
   private String reason;
   
   public StoreHolidayRequest(StoreHolidays e) {
	   this.holidayDate = e.getHolidayDate();
	   this.isRecurring = e.getIsRecurring();
	   this.reason      = e.getReason();
	 }




}
