package com.airtribe.parkinglot.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:api-test")
@AutoConfigureMockMvc
class ParkingApiTest {
    @Autowired
    MockMvc mvc;

    @Test
    void checkInThenCheckOutThenDuplicateConflicts() throws Exception {
        mvc.perform(post("/api/tickets").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"plate\":\"ka01api1\",\"type\":\"CAR\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.vehicle.plate").value("KA01API1"));
        mvc.perform(post("/api/tickets").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"plate\":\"KA01API1\",\"type\":\"CAR\"}"))
                .andExpect(status().isConflict());
        mvc.perform(get("/api/availability")).andExpect(status().isOk());
    }

    @Test
    void unknownTicketIs404AndBadBodyIs400() throws Exception {
        mvc.perform(post("/api/tickets/NOPE/checkout")).andExpect(status().isNotFound());
        mvc.perform(post("/api/tickets").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"plate\":\"\",\"type\":\"CAR\"}"))
                .andExpect(status().isBadRequest());
    }
}
