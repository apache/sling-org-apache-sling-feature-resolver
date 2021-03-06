/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.sling.feature.resolver.impl;

import org.apache.felix.utils.resource.CapabilityImpl;
import org.apache.felix.utils.resource.RequirementImpl;
import org.apache.sling.feature.Artifact;
import org.apache.sling.feature.Feature;
import org.apache.sling.feature.resolver.FeatureResource;
import org.osgi.framework.Version;
import org.osgi.framework.namespace.IdentityNamespace;
import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeatureResourceImpl extends AbstractResourceImpl implements FeatureResource {
    private final Artifact artifact;
    private final Feature feature;
    private final Map<String, List<Capability>> capabilities;
    private final Map<String, List<Requirement>> requirements;

    public FeatureResourceImpl(Feature f) {
        artifact = new Artifact(f.getId());
        feature = f;

        capabilities = new HashMap<>();
        for (Capability r : f.getCapabilities()) {
            List<Capability> l = capabilities.get(r.getNamespace());
            if (l == null) {
                l = new ArrayList<>();
                capabilities.put(r.getNamespace(), l);
            }
            l.add(new CapabilityImpl(this, r));
        }

        // Add the identity capability
        Map<String, Object> idattrs = new HashMap<>();
        idattrs.put(IdentityNamespace.IDENTITY_NAMESPACE, getId());
        idattrs.put(IdentityNamespace.CAPABILITY_TYPE_ATTRIBUTE, "sling.feature");
        idattrs.put(IdentityNamespace.CAPABILITY_VERSION_ATTRIBUTE, getVersion());
        idattrs.put(IdentityNamespace.CAPABILITY_DESCRIPTION_ATTRIBUTE, f.getDescription());
        idattrs.put(IdentityNamespace.CAPABILITY_LICENSE_ATTRIBUTE, f.getLicense());
        Capability idCap = new CapabilityImpl(this, IdentityNamespace.IDENTITY_NAMESPACE, null, idattrs);
        capabilities.put(IdentityNamespace.IDENTITY_NAMESPACE, Collections.singletonList(idCap));

        requirements = new HashMap<>();
        for (Requirement r : f.getRequirements()) {
            List<Requirement> l = requirements.get(r.getNamespace());
            if (l == null) {
                l = new ArrayList<>();
                requirements.put(r.getNamespace(), l);
            }
            l.add(new RequirementImpl(this, r));
        }
    }

    @Override
    public String getId() {
        return artifact.getId().getArtifactId();
    }

    @Override
    public Version getVersion() {
        return artifact.getId().getOSGiVersion();
    }

    @Override
    public Artifact getArtifact() {
        return artifact;
    }

    @Override
    public Feature getFeature() {
        return feature;
    }

    @Override
    public List<Capability> getCapabilities(String namespace) {
        return super.getCapabilities(namespace, capabilities);
    }

    @Override
    public List<Requirement> getRequirements(String namespace) {
        return super.getRequirements(namespace, requirements);
    }

    @Override
    public int hashCode() {
        return feature.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FeatureResourceImpl) {
            return feature.equals(((FeatureResourceImpl) obj).feature);
        }
        return false;
    }

    @Override
    public String toString() {
        return "FeatureResourceImpl [artifact=" + artifact + "]";
    }
}
