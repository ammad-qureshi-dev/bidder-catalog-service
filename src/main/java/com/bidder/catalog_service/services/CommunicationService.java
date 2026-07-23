/* (C) 2026
bidder.app */
package com.bidder.catalog_service.services;

import com.bidder.catalog_service.models.CommsStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CommunicationService {

	// ToDo: dummy for sending emails
	public CommsStatus sendCommunication(String message) {
		log.debug("CommsService -- {}", message);
		return CommsStatus.SENT;
	}
}
