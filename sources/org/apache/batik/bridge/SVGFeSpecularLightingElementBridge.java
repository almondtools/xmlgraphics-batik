/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included with this distribution in  *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.batik.bridge;

import java.awt.geom.Rectangle2D;
import java.util.Map;

import org.apache.batik.ext.awt.image.Light;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.ext.awt.image.renderable.SpecularLightingRable8Bit;
import org.apache.batik.ext.awt.image.renderable.SpecularLightingRable;
import org.apache.batik.gvt.GraphicsNode;

import org.w3c.dom.Element;

/**
 * Bridge class for the &lt;feSpecularLighting> element.
 *
 * @author <a href="mailto:tkormann@apache.org">Thierry Kormann</a>
 * @version $Id$
 */
public class SVGFeSpecularLightingElementBridge
    extends SVGFeAbstractLightingElementBridge {


    /**
     * Constructs a new bridge for the &lt;feSpecularLighting> element.
     */
    public SVGFeSpecularLightingElementBridge() {}

    /**
     * Creates a <tt>Filter</tt> primitive according to the specified
     * parameters.
     *
     * @param ctx the bridge context to use
     * @param filterElement the element that defines a filter
     * @param filteredElement the element that references the filter
     * @param filteredNode the graphics node to filter
     *
     * @param inputFilter the <tt>Filter</tt> that represents the current
     *        filter input if the filter chain.
     * @param filterRegion the filter area defined for the filter chain
     *        the new node will be part of.
     * @param filterMap a map where the mediator can map a name to the
     *        <tt>Filter</tt> it creates. Other <tt>FilterBridge</tt>s
     *        can then access a filter node from the filterMap if they
     *        know its name.
     */
    public Filter createFilter(BridgeContext ctx,
                               Element filterElement,
                               Element filteredElement,
                               GraphicsNode filteredNode,
                               Filter inputFilter,
                               Rectangle2D filterRegion,
                               Map filterMap) {


        // 'surfaceScale' attribute - default is 1
        float surfaceScale
            = convertNumber(filterElement, SVG_SURFACE_SCALE_ATTRIBUTE, 1);

        // 'specularConstant' attribute - default is 1
        float specularConstant
            = convertNumber(filterElement, SVG_SPECULAR_CONSTANT_ATTRIBUTE, 1);

        // 'specularExponent' attribute - default is 1
        float specularExponent = convertSpecularExponent(filterElement);

        // extract the light definition from the filterElement's children list
        Light light = extractLight(filterElement, ctx);

        // 'kernelUnitLength' attribute
        double [] kernelUnitLength = convertKernelUnitLength(filterElement);

        // 'in' attribute
        Filter in = getIn(filterElement,
                          filteredElement,
                          filteredNode,
                          inputFilter,
                          filterMap,
                          ctx);
        if (in == null) {
            return null; // disable the filter
        }

        // The default region is the union of the input sources
        // regions unless 'in' is 'SourceGraphic' in which case the
        // default region is the filterChain's region
        Filter sourceGraphics = (Filter)filterMap.get(SVG_SOURCE_GRAPHIC_VALUE);
        Rectangle2D defaultRegion;
        if (in == sourceGraphics) {
            defaultRegion = filterRegion;
        } else {
            defaultRegion = in.getBounds2D();
        }

        Rectangle2D primitiveRegion
            = SVGUtilities.convertFilterPrimitiveRegion(filterElement,
                                                        filteredElement,
                                                        filteredNode,
                                                        defaultRegion,
                                                        filterRegion,
                                                        ctx);

        Filter filter = new  SpecularLightingRable8Bit(in,
                                                       primitiveRegion,
                                                       light,
                                                       specularConstant,
                                                       specularExponent,
                                                       surfaceScale,
                                                       kernelUnitLength);


        // update the filter Map
        updateFilterMap(filterElement, filter, filterMap);

        return filter;
    }

    /**
     * Returns the specular exponent of the specular feSpecularLighting
     * filter primitive element.
     *
     * @param filterElement the feSpecularLighting filter primitive element
     */
    protected static float convertSpecularExponent(Element filterElement) {
        String s = filterElement.getAttributeNS
            (null, SVG_SPECULAR_EXPONENT_ATTRIBUTE);
        if (s.length() == 0) {
            return 1; // default is 1
        } else {
            try {
                float v = SVGUtilities.convertSVGNumber(s);
                if (v < 1 || v > 128) {
                    throw new BridgeException
                        (filterElement, ERR_ATTRIBUTE_VALUE_MALFORMED,
                         new Object[] {SVG_SPECULAR_CONSTANT_ATTRIBUTE, s});
                }
                return v;
            } catch (NumberFormatException ex) {
                throw new BridgeException
                    (filterElement, ERR_ATTRIBUTE_VALUE_MALFORMED,
                     new Object[] {SVG_SPECULAR_CONSTANT_ATTRIBUTE, s, ex});
            }
        }
    }
}
