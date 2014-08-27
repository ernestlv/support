package com.scrippsnetworks.wcm.map.util;

/**
 * Created with IntelliJ IDEA.
 * User: idemeshkevich
 * Date: 17.09.13
 * Time: 15:26
 * To change this template use File | Settings | File Templates.
 */


import com.day.cq.commons.jcr.JcrUtil;

import java.util.*;

/**
 * This view helper returns a list of states
 *
 */
public class StateHelper {

    /**
     * Objects are returned of type State
     */
    public class State {
        private String code;
        private String name;
        protected State(String code, String name) {
            this.code=code;
            this.name=name;
        }
        public String getCode() {
            return code;
        }
        public String getName() {
            return name;
        }
        public String getValidJcrName(){
            return JcrUtil.createValidName(name);
        }
    }

    private List<State> states = null;

    private static Map<String, String> statesMap = new LinkedHashMap<String, String>();

    static{
        statesMap.put("AL","Alabama");
        statesMap.put("AK","Alaska");
        statesMap.put("AZ","Arizona");
        statesMap.put("AR","Arkansas");
        statesMap.put("CA","California");
        statesMap.put("CO","Colorado");
        statesMap.put("CT","Connecticut");
        statesMap.put("DC","DC");
        statesMap.put("DE","Delaware");
        statesMap.put("FL","Florida");
        statesMap.put("GA","Georgia");
        statesMap.put("HI","Hawaii");
        statesMap.put("ID","Idaho");
        statesMap.put("IL","Illinois");
        statesMap.put("IN","Indiana");
        statesMap.put("IA","Iowa");
        statesMap.put("KS","Kansas");
        statesMap.put("KY","Kentucky");
        statesMap.put("LA","Louisiana");
        statesMap.put("ME","Maine");
        statesMap.put("MD","Maryland");
        statesMap.put("MA","Massachusetts");
        statesMap.put("MI","Michigan");
        statesMap.put("MN","Minnesota");
        statesMap.put("MS","Mississippi");
        statesMap.put("MO","Missouri");
        statesMap.put("MT","Montana");
        statesMap.put("NE","Nebraska");
        statesMap.put("NV","Nevada");
        statesMap.put("NH","New Hampshire");
        statesMap.put("NJ","New Jersey");
        statesMap.put("NM","New Mexico");
        statesMap.put("NY","New York");
        statesMap.put("NC","North Carolina");
        statesMap.put("ND","North Dakota");
        statesMap.put("OH","Ohio");
        statesMap.put("OK","Oklahoma");
        statesMap.put("OR","Oregon");
        statesMap.put("PA","Pennsylvania");
        statesMap.put("RI","Rhode Island");
        statesMap.put("SC","South Carolina");
        statesMap.put("SD","South Dakota");
        statesMap.put("TN","Tennessee");
        statesMap.put("TX","Texas");
        statesMap.put("UT","Utah");
        statesMap.put("VT","Vermont");
        statesMap.put("VA","Virginia");
        statesMap.put("WA","Washington");
        statesMap.put("WV","West Virginia");
        statesMap.put("WI","Wisconsin");
        statesMap.put("WY","Wyoming");
    }

    public static String getStateByCode(String code){
        String state = statesMap.get(code);

        if (state != null){
            return state;
        }

        return "";
    }

    public Set<Map.Entry<String, String>> getAllStates() {
        return statesMap.entrySet();
    }
}