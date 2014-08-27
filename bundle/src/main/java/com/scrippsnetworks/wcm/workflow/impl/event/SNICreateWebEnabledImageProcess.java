/*
* Copyright 1997-2008 Day Management AG
* Barfuesserplatz 6, 4001 Basel, Switzerland
* All Rights Reserved.
*
* This software is the confidential and proprietary information of
* Day Management AG, ("Confidential Information"). You shall not
* disclose such Confidential Information and shall use it only in
* accordance with the terms of the license agreement you entered into
* with Day.
*/
package com.scrippsnetworks.wcm.workflow.impl.event;

import  com.scrippsnetworks.wcm.workflow.util.SNIWebEnabledImageCreator;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.Rendition;
import com.day.cq.dam.api.cache.BufferedImageCache;
import com.day.cq.dam.api.handler.AssetHandler;
import com.day.cq.dam.commons.process.AbstractAssetWorkflowProcess;
import com.day.cq.dam.commons.util.MemoryUtil;
import com.day.cq.dam.commons.util.WebEnabledImageCreator;
import com.day.cq.workflow.WorkflowException;
import com.day.cq.workflow.WorkflowSession;
import com.day.cq.workflow.exec.WorkItem;
import com.day.cq.workflow.metadata.MetaDataMap;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.IIOException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
* The <code>SNICreateWebEnabledImageProcess</code> is called in a Workflow
* {@link com.day.cq.workflow.exec.WorkflowProcess Process} step. This Process
* creates, if possible, a web enabled representation from the
* {@link com.day.cq.dam.api.Asset Asset}.<br>
* The Format of the webenabled image, can be set by arguments to the
* {@link com.day.cq.workflow.exec.WorkflowProcess#execute(WorkItem, WorkflowSession, MetaDataMap)
* Process}<br>
* Process is memory aware. If the required memory is not available, image
* creatation is deferred or cancelled if the memory requirement can't be
* sattisfied within a fixed amount of trails.
*
* @see AbstractAssetWorkflowProcess
*/
@Component(metatype = false)
@Service
@Property(name = "process.label", value = "SNI Custom Create Web Enabled Image")
public class SNICreateWebEnabledImageProcess extends AbstractAssetWorkflowProcess {

    /**
* Logger instance for this class.
*/
    private static final Logger log = LoggerFactory.getLogger(SNICreateWebEnabledImageProcess.class);

    /**
* Maximum number of trials to create a Web Enabled Image.
*/
    private static final int MAX_TRIALS = 100;

    @Reference(policy = ReferencePolicy.STATIC)
    private BufferedImageCache imageCache;

    /**
* The available arguments to this process implementation.
*/
    public enum Arguments {
        PROCESS_ARGS("PROCESS_ARGS"), DIMENSION("dimension"), WIDTH("width"), HEIGHT("height"), QUALITY("quality"), MIME_TYPE(
                "mimetype"), SKIP("skip"), KEEP_FORMAT_LIST("keepFormatList");

        private String argumentName;

        Arguments(String argumentName) {
            this.argumentName = argumentName;
        }

        public String getArgumentName() {
            return this.argumentName;
        }

        public String getArgumentPrefix() {
            return this.argumentName + ":";
        }

    }

    public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaData)
            throws WorkflowException {
        String[] args = buildArguments(metaData);

        try {

            final Asset asset = getAssetFromPayload(workItem, workflowSession.getSession());

            if (null != asset && !doIgnore(args, asset)) {
                asset.setBatchMode(true);

                // read workflow process arguments
                // dimensions
                final String dimensions = getValuesFromArgs("dimension", args).size() > 0 ? getValuesFromArgs(
                        "dimension", args).get(0) : null;

                // default mime type
                final String mimetype = getValuesFromArgs("mimetype", args).size() > 0 ? getValuesFromArgs("mimetype",
                        args).get(0) : "image/png";

                // keep the original format?
                final String keepFormat = getValuesFromArgs("keepFormatList", args).size() > 0 ? getValuesFromArgs(
                        "keepFormatList", args).get(0)
                        : "image/pjpeg,image/jpeg,image/jpg,image/gif,image/png,image/x-png";

                // image quality: from 0 t0 100%
                final String qualityStr = getValuesFromArgs("quality", args).size() > 0 ? getValuesFromArgs("quality",
                        args).get(0) : "60";

                if (MemoryUtil.hasEnoughSystemMemory(asset)) {

                    // fetch buffered image
                    final AssetHandler handler = getAssetHandler(asset.getMimeType());
                    final SNIWebEnabledImageCreator creator = new SNIWebEnabledImageCreator(asset, mimeTypeService);
                    final Rendition original = asset.getOriginal();

                    // retry to load image if there is not enough memory
                    // currently
                    // available.
                    // did not find a more elegant solution by now...
                    BufferedImageCache.Entry image = null;
                    long trials = MAX_TRIALS;
                    try {
                        while (image == null && trials > 0) {
                            trials--;
                            try {
                                image = imageCache.getImage(original, handler);
                            } catch (IOException e) {
                                if (e instanceof IIOException && e.getMessage().contains("Not enough memory")) {
                                    log.debug(
                                            "execute: insufficient memory, reloading image. Free mem [{}]. Asset [{}].",
                                            Runtime.getRuntime().freeMemory(), asset.getPath());
                                    Thread.sleep((long) (5000 * (Math.random() + 2)));
                                    continue;
                                } else {
                                    log.debug("execute: error while loading image for [{}]: ", asset.getPath(), e);
                                    throw new IOException(e.getMessage());
                                }
                            }

                            if (image != null) {
                                try {
                                    // need to force creation of new images,
                                    // since
                                    // image from cache
                                    // might already be smaller
                                    creator.create(image.getImage(), mimetype, dimensions, keepFormat, qualityStr, true);
                                } catch (IOException e) {
                                    if (e instanceof IIOException && e.getMessage().contains("Not enough memory")) {
                                        if (log.isDebugEnabled()) {
                                            log.debug(
                                                    "execute: insufficient memory, reloading image. Free mem [{}]. Asset [{}].",
                                                    Runtime.getRuntime().freeMemory(), asset.getPath());
                                        }
                                        image.release();
                                        image = null;
                                        // sleep at least 10000ms and maximal
                                        // 15000ms
                                        Thread.sleep((long) (5000 * (Math.random() + 2)));
                                    } else {
                                        log.debug("execute: error while loading web enabled image for [{}]: ",
                                                asset.getPath(), e);
                                        throw new IOException(e.getMessage());
                                    }
                                }
                            } else {
                                log.warn("execute: cannot extract image from [{}].", asset.getPath());
                                break;
                            }
                        }
                    } finally {
                        if (image != null) {
                            image.release();
                        }
                    }
                    if (trials == 0) {
                        log.warn(
                                "execute: failed creating web enabled image, insufficient memory even after [{}] trials for [{}].",
                                MAX_TRIALS, asset.getPath());
                    }
                } else {
                    log.warn(
                            "execute: failed loading image, insufficient memory. Increase heap size up to [{}bytes] for asset [{}].",
                            MemoryUtil.suggestMaxHeapSize(asset), asset.getPath());
                }

            } else {
             if (asset == null) {
             String wfPayload = workItem.getWorkflowData().getPayload().toString();
String message = "execute: cannot create web enabled image, asset [{" + wfPayload + "}] in payload doesn't exist for workflow [{" + workItem.getId() + "}].";
throw new WorkflowException(message);
             } else {
             log.debug("execute: asset [{}] exists, but configured to ignore.",asset.getPath());
             }
            }
        } catch (Exception e) {
            throw new WorkflowException(e);
        }
    }

    // -------------< helper
    // >---------------------------------------------------

    private boolean doIgnore(String[] args, Asset asset) {
        final String mimeType = asset.getMimeType();
        if (mimeType == null) {
            // ignore since no mimetype is available
            log.debug("doIgnore: no mimetype available for asset [{}].", asset.getPath());
            return true;
        }
        List<String> values = getValuesFromArgs("skip", args);
        for (String val : values) {

            if (mimeType.matches(val)) {
                return true;
            }
        }
        return false;
    }

    public String[] buildArguments(MetaDataMap metaData) {

        // the 'old' way, ensures backward compatibility
        String processArgs = metaData.get(Arguments.PROCESS_ARGS.name(), String.class);
        if (processArgs != null && !processArgs.equals("")) {
            return processArgs.split(",");
        }
        // the 'new' way
        else {
            List<String> arguments = new ArrayList<String>();

            String width = metaData.get(Arguments.WIDTH.name(), String.class);
            String height = metaData.get(Arguments.HEIGHT.name(), String.class);
            if (StringUtils.isNotBlank(width) && StringUtils.isNotBlank(height)) {
                StringBuilder builder = new StringBuilder();
                builder.append(Arguments.DIMENSION.getArgumentPrefix()).append(width).append(":").append(height);
                arguments.add(builder.toString());
            }

            String value = metaData.get(Arguments.QUALITY.name(), String.class);
            if (StringUtils.isNotBlank(value)) {
                StringBuilder builder = new StringBuilder();
                builder.append(Arguments.QUALITY.getArgumentPrefix()).append(value);
                arguments.add(builder.toString());

            }

            value = metaData.get(Arguments.MIME_TYPE.name(), String.class);
            if (StringUtils.isNotBlank(value)) {
                StringBuilder builder = new StringBuilder();
                builder.append(Arguments.MIME_TYPE.getArgumentPrefix()).append(value);
                arguments.add(builder.toString());
            }

            String[] skipValues = metaData.get(Arguments.SKIP.name(), String[].class);
            if (skipValues != null) {
                for (String skipValue : skipValues) {
                    StringBuilder builder = new StringBuilder();
                    builder.append(Arguments.SKIP.getArgumentPrefix()).append(skipValue);
                    arguments.add(builder.toString());
                }
            }

            String[] keepValues = metaData.get(Arguments.KEEP_FORMAT_LIST.name(), String[].class);
            if (keepValues != null) {
                StringBuilder builder = new StringBuilder();
                builder.append(Arguments.KEEP_FORMAT_LIST.getArgumentPrefix());
                for (int i = 0; i < keepValues.length; i++) {
                    builder.append(keepValues[i]);
                    if (i < keepValues.length - 1) {
                        builder.append(",");
                    }
                }
                arguments.add(builder.toString());
            }
            return arguments.toArray(new String[arguments.size()]);
        }
    }

}