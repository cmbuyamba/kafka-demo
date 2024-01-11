package com.ugovia.kafka.kafkademo.client;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import com.ugovia.kafka.kafkademo.model.EventLog;

/**
 * 
 * @author Celestin Mbuyamba
 */

public class SnmpGet {
    private static Logger log = LoggerFactory.getLogger(SnmpGet.class);
    public static final int DEFAULT_VERSION = SnmpConstants.version2c;
    public static final String DEFAULT_PROTOCOL = "udp";
    public static final int DEFAULT_PORT = 161;
    public static final long DEFAULT_TIMEOUT = 3 * 1000L;
    public static final int DEFAULT_RETRY = 3;

    private SnmpGet() {
    }

    /**
     * 
     * @param ip
     * @param community
     * @return CommunityTarget
     */
    public static CommunityTarget<Address> createDefault(String ip, String community) {
        Address address = GenericAddress.parse(DEFAULT_PROTOCOL + ":" + ip + "/" + DEFAULT_PORT);
        CommunityTarget<Address> target = new CommunityTarget<>();
        target.setCommunity(new OctetString(community));
        target.setAddress(address);
        target.setVersion(DEFAULT_VERSION);
        target.setTimeout(DEFAULT_TIMEOUT); // milliseconds
        target.setRetries(DEFAULT_RETRY);
        return target;
    }

    public static List<EventLog> snmpGet(String ip, String community, String oid) {

        CommunityTarget target = createDefault(ip, community);
        List<EventLog> result = new ArrayList<>();
        try (
                DefaultUdpTransportMapping transport = new DefaultUdpTransportMapping();
                Snmp snmp = new Snmp(transport);
                ) {
            PDU pdu = new PDU();
            pdu.add(new VariableBinding(new OID(oid)));

            snmp.listen();
            log.info("-------> PDU <-------");
            pdu.setType(PDU.GET);
            ResponseEvent respEvent = snmp.send(pdu, target);
            log.info("PeerAddress: {}", respEvent.getPeerAddress());
            PDU response = respEvent.getResponse();

            if (response == null) {
                log.info("response is null, request time out");
            } else {

                log.info("response pdu size is {}", response.size());
                for (int i = 0; i < response.size(); i++) {
                    VariableBinding vb = response.get(i);
                    result.add(new EventLog(vb.getOid().toString(), vb.getVariable().toString(), ip));
                    log.info("{} = {} ", vb.getOid(), vb.getVariable());
                }

            }
            log.info("SNMP GET one OID value finished !");
        } catch (Exception e) {
            e.printStackTrace();
            log.info("SNMP Get Exception: {}", e.getMessage());
        }
        return result;
    }
}
