package edu.college.transport.dto;

import edu.college.transport.entity.Bus;

public record NearbyBusSuggestion(Bus bus, long seatsAvailable, Integer etaMinutes, String availability) {
    public String etaLabel() {
        return etaMinutes == null ? availability : "About " + etaMinutes + " min to your stop";
    }
}
