package com.supernovapos.finalproject.auth.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PermissionEnum {

	// ================= User permissions =================
	USER_READ("USER_READ", "GET", "/api/users/**", "View user information", "USER"),
	USER_CREATE("USER_CREATE", "POST", "/api/users", "Create a new user", "USER"),
	USER_UPDATE("USER_UPDATE", "PUT", "/api/users/{id}", "Update user information", "USER"),
	USER_DELETE("USER_DELETE", "DELETE", "/api/users/{id}", "Delete a user", "USER"),

	// ================= Staff permissions =================
	STAFF_READ("STAFF_READ", "GET", "/api/staff/**", "View staff information", "STAFF"),
	STAFF_CREATE("STAFF_CREATE", "POST", "/api/staff", "Create a new staff account", "STAFF"),
	STAFF_UPDATE("STAFF_UPDATE", "PUT", "/api/staff/{id}", "Update staff information", "STAFF"),
	STAFF_DELETE("STAFF_DELETE", "DELETE", "/api/staff/{id}", "Delete a staff account", "STAFF"),

	// ================= Store permissions =================
	STORE_READ("STORE_READ", "GET", "/api/store/**", "View store information", "STORE"),
	STORE_UPDATE("STORE_UPDATE", "PUT", "/api/store/**", "Update store information", "STORE"),
	STORE_ADMIN_READ("STORE_ADMIN_READ", "GET", "/api/store/admin", "View store settings for admin", "STORE"),

	// ================= DISCORD permissions =================
	DISCORD_SEND("DISCORD_SEND", "POST", "/api/discord/send", "Send message to Discord", "DISCORD"),
	DISCORD_DEBUG("DISCORD_DEBUG", "POST", "/api/discord/debug/sendTest", "Send test debug message to Discord",
			"DISCORD"),

	// ================= BUSINESSHOUR permissions =================
	BUSINESSHOUR_LIST("BUSINESSHOUR_LIST", "GET", "/api/businessHours/list", "List business hours", "BUSINESSHOUR"),
	BUSINESSHOUR_CREATE("BUSINESSHOUR_CREATE", "POST", "/api/businessHours/create", "Create business hours",
			"BUSINESSHOUR"),
	BUSINESSHOUR_UPDATE("BUSINESSHOUR_UPDATE", "PUT", "/api/businessHours/update/{id}", "Update business hours",
			"BUSINESSHOUR"),
	BUSINESSHOUR_DELETE("BUSINESSHOUR_DELETE", "DELETE", "/api/businessHours/delete/{id}", "Delete business hours",
			"BUSINESSHOUR"),

	// ================= POSRESERVATION permissions =================
	POS_CREATE("POS_CREATE", "POST", "/api/reservations/pos/create", "Create reservation (POS)", "POSRESERVATION"),
	POS_UPDATE("POS_UPDATE", "PUT", "/api/reservations/pos/update/{id}", "Update reservation (POS)", "POSRESERVATION"),
	POS_GETAVAILABLETIME("POS_GETAVAILABLETIME", "GET", "/api/reservations/pos/available-time",
			"Get available reservation times (POS)", "POSRESERVATION"),
	POS_LIST("POS_LIST", "GET", "/api/reservations/pos/list", "List reservations (POS)", "POSRESERVATION"),
	POS_DELETE("POS_DELETE", "DELETE", "/api/reservations/pos/delete/{id}", "Delete reservation (POS)",
			"POSRESERVATION"),
	POS_CHECKIN("POS_CHECKIN", "PUT", "/api/reservations/pos/checkin/{id}", "Check-in reservation (POS)",
			"POSRESERVATION"),

	// ================= STOREHOLIDAYS permissions =================
	STOREHOLIDAYS_CREATE("STOREHOLIDAYS_CREATE", "POST", "/api/storeholidays/create", "Create store holiday",
			"STOREHOLIDAYS"),
	STOREHOLIDAYS_UPDATE("STOREHOLIDAYS_UPDATE", "PUT", "/api/storeholidays/update/{id}", "Update store holiday",
			"STOREHOLIDAYS"),
	STOREHOLIDAYS_LIST("STOREHOLIDAYS_LIST", "GET", "/api/storeholidays/list", "List store holidays", "STOREHOLIDAYS"),
	STOREHOLIDAYS_DELETE("STOREHOLIDAYS_DELETE", "DELETE", "/api/storeholidays/delete/{id}", "Delete store holiday",
			"STOREHOLIDAYS"),

	// ================= TABLE permissions =================
	TABLE_CREATE("TABLE_CREATE", "POST", "/api/restaurantTable/create", "Create table", "TABLE"),
	TABLE_LIST("TABLE_LIST", "GET", "/api/restaurantTable/list", "List tables", "TABLE"),
	TABLE_GETID("TABLE_GETID", "GET", "/api/restaurantTable/{id}", "Get table by id", "TABLE"),
	TABLE_GETEMPTY("TABLE_GETEMPTY", "GET", "/api/restaurantTable/empty", "List empty tables", "TABLE"),
	TABLE_UPDATESTATUS("TABLE_UPDATESTATUS", "PUT", "/api/restaurantTable/{id}/status", "Update table status", "TABLE"),
	TABLE_GETNEXT("TABLE_GETNEXT", "GET", "/api/restaurantTable/{tableId}/next", "Get next table info", "TABLE"),
	TABLE_UPDATEINFO("TABLE_UPDATEINFO", "PUT", "/api/restaurantTable/{id}/info", "Update table info", "TABLE"),
	TABLE_UPDATELAYOUT("TABLE_UPDATELAYOUT", "PUT", "/api/restaurantTable/layout", "Update table layout", "TABLE"),

	// ================= Analytics permissions =================
	ANALYTICS_READ("ANALYTICS_READ","GET","/api/analytics/product/**","View product analytics reports","ANALYTICS"),
	ANALYTICS_SEARCH("ANALYTICS_SEARCH","POST","/api/analytics/product/search","Search product analytics reports","ANALYTICS");

	private final String code;
	private final String httpMethod;
	private final String url;
	private final String description;
	private final String categoryName;
}
