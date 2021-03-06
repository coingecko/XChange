package com.xeiam.xchange.lakebtc.service;

import com.xeiam.xchange.lakebtc.LakeBTCUtil;
import com.xeiam.xchange.lakebtc.dto.LakeBTCRequest;
import com.xeiam.xchange.service.BaseParamsDigest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import si.mazi.rescu.BasicAuthCredentials;
import si.mazi.rescu.RestInvocation;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * User: cristian.lucaci
 * Date: 10/3/2014
 * Time: 5:03 PM
 */
public class LakeBTCDigest extends BaseParamsDigest {

    private final Logger log = LoggerFactory.getLogger(LakeBTCDigest.class);

    private static final String FIELD_SEPARATOR = "\",\"";

    private final String clientId;
    private final String apiKey;

    /**
     * Constructor
     *
     * @param secretKeyBase64 secretKeyBase64 key
     * @param clientId        client ID, mail
     * @param secretKeyBase64 @throws IllegalArgumentException if key is invalid (cannot be base-64-decoded or the decoded key is invalid).
     */
    private LakeBTCDigest(String clientId, String secretKeyBase64) {

        super(secretKeyBase64, HMAC_SHA_1);
        this.clientId = clientId;
        this.apiKey = secretKeyBase64;
    }

    public static LakeBTCDigest createInstance(String clientId, String secretKeyBase64) {
        return secretKeyBase64 == null ? null : new LakeBTCDigest(clientId, secretKeyBase64);
    }


    @Override
    public String digestParams(RestInvocation restInvocation) {

        String tonce = restInvocation.getHttpHeadersFromParams().get("Json-Rpc-Tonce");

        LakeBTCRequest request = null;
        for (Object param : restInvocation.getUnannanotatedParams()) {
            if (param instanceof LakeBTCRequest) {
                request = (LakeBTCRequest) param;
            }
        }

        if (request == null) {
            throw new IllegalArgumentException("No LakeBTCDigest found.");
        }

        final long id = request.getId();
        final String method = request.getRequestMethod();
        final String params = ""; //stripParams(request.getParams());

        String signature = String.format("tonce=%s&accesskey=%s&requestmethod=%s&id=%d&method=%s&params=%s", tonce, clientId, method, id,
                request.getMethod(), params);
        log.debug("signature message: {}", signature);


        Mac mac = getMac();
        byte[] hash = mac.doFinal(signature.getBytes());

        BasicAuthCredentials auth = new BasicAuthCredentials(apiKey, LakeBTCUtil.bytesToHex(hash));

        return auth.digestParams(restInvocation);
    }

    /**
     * Strip the {@code params} for signature message.
     *
     */
    private String stripParams(String params) {

        final String[] original = params.substring(1, params.length() - 1).split(",");
        final String[] stripped = new String[original.length];

        for (int i = 0; i < original.length; i++) {
            final String param = original[i];

            if (param.startsWith("\"") && param.endsWith("\"")) {
                // string
                stripped[i] = param.substring(1, param.length() - 1);
            }
            else if (param.equals("true")) {
                // boolean: true
                stripped[i] = "1";
            }
            else if (param.equals("false")) {
                // boolean: false
                stripped[i] = StringUtils.EMPTY;
            }
            else if (param.equals("null")) {
                stripped[i] = StringUtils.EMPTY;
            }
            else {
                // number, etc.
                stripped[i] = param;
            }

        }
        return StringUtils.join(stripped, ",");
    }

    public static String makeSign(String data, String key) throws Exception {

        SecretKeySpec sign = new SecretKeySpec(key.getBytes(), HMAC_SHA_1);
        Mac mac = Mac.getInstance(HMAC_SHA_1);
        mac.init(sign);
        byte[] rawHmac = mac.doFinal(data.getBytes());

        return arrayHex(rawHmac);
    }

    private static String arrayHex(byte[] a) {
        StringBuilder sb = new StringBuilder();
        for (byte b : a)
            sb.append(String.format("%02x", b & 0xff));
        return sb.toString();
    }

    public static void main(String[] args) {
        String params = "tonce=1389067414466757&accesskey=foo@bar.com&requestmethod=post&id=123&method=ticker&params=";

    }
}
