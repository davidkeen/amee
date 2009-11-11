package com.amee.calculation.service;

import com.amee.core.ThreadBeanHolder;
import com.amee.domain.AMEEStatistics;
import com.amee.domain.algorithm.Algorithm;
import com.amee.domain.data.ItemDefinition;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Map;

/**
 * This file is part of AMEE.
 * <p/>
 * AMEE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * AMEE is free software and is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * <p/>
 * Created by http://www.dgen.net.
 * Website http://www.amee.cc
 */

@Service
public class AlgorithmService {

    private final Log log = LogFactory.getLog(getClass());

    // The ScriptEngine for the Javscript context.
    private final ScriptEngine engine = new ScriptEngineManager().getEngineByName("js");

    // Default Algorithm name to use in calculations
    private final static String DEFAULT = "default";

    @Autowired
    private AMEEStatistics ameeStatistics;

    /**
     * Get the default algorithm for an ItemDefinition.
     *
     * @param itemDefinition
     * @return the default Algorithm for the supplied ItemDefinition
     */
    public Algorithm getAlgorithm(ItemDefinition itemDefinition) {
        return itemDefinition.getAlgorithm(DEFAULT);
    }

    /**
     * Evaluate an Algorithm.
     *
     * @param algorithm - the Algorithm to evaluate
     * @param values    - map of key/value input pairs
     * @return the value returned by the Algorithm as a String
     * @throws ScriptException - rethrows exception generated by script execution
     */
    public String evaluate(Algorithm algorithm, Map<String, Object> values) throws ScriptException {
        final long startTime = System.nanoTime();
        try {
            Bindings bindings = createBindings();
            bindings.putAll(values);
            bindings.put("logger", log);
            Object result = getCompiledScript(algorithm).eval(bindings);
            if (result != null) {
                return result.toString();
            } else {
                throw new RuntimeException("Result from CompiledScript.eval() was null. Algorithm UID is: " + algorithm.getUid());
            }
        } finally {
            ameeStatistics.addToThreadCalculationDuration(System.nanoTime() - startTime);
        }
    }

    private Bindings createBindings() {
        return engine.createBindings();
    }

    /**
     * Return a CompiledScript based on the content of the Algorithm. Will throw a RuntimeException if
     * the Algorithm content is blank or if the CompiledScript is null. Will cache the CompiledScript
     * in the thread.
     *
     * @param algorithm to create CompiledScript from
     * @return the CompiledScript
     * @throws ScriptException
     */
    private CompiledScript getCompiledScript(Algorithm algorithm) throws ScriptException {
        CompiledScript compiledScript = (CompiledScript) ThreadBeanHolder.get("algorithm-" + algorithm.getUid());
        if (compiledScript == null) {
            if (StringUtils.isBlank(algorithm.getContent())) {
                throw new RuntimeException("Algorithm content is null. Algorithm UID is: " + algorithm.getUid());
            }
            compiledScript = ((Compilable) engine).compile(algorithm.getContent());
            if (compiledScript == null) {
                throw new RuntimeException("CompiledScript is null. Algorithm UID is: " + algorithm.getUid());
            }
            ThreadBeanHolder.set("algorithm-" + algorithm.getUid(), compiledScript);
        }
        return compiledScript;
    }
}
