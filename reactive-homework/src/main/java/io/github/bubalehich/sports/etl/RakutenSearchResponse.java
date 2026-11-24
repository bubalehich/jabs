package io.github.bubalehich.sports.etl;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

record RakutenSearchResponse(@JsonProperty("Items") List<Item> items) {

	record Item(String itemName) {
	}

}
