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
import org.apache.sling.feature.scanner.BundleDescriptor;
import org.apache.sling.feature.scanner.PackageInfo;
import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;
import org.osgi.framework.namespace.BundleNamespace;
import org.osgi.framework.namespace.ExecutionEnvironmentNamespace;
import org.osgi.framework.namespace.IdentityNamespace;
import org.osgi.framework.namespace.PackageNamespace;
import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the OSGi Resource interface.
 */
public class BundleResourceImpl extends AbstractResourceImpl implements FeatureResource {
    final Artifact artifact;
    final String bsn;
    final Version version;
    final Map<String, List<Capability>> capabilities;
    final Map<String, List<Requirement>> requirements;
    final Feature feature;

    /**
     * Create a resource based on a BundleDescriptor.
     * @param bd The BundleDescriptor to represent.
     */
    public BundleResourceImpl(BundleDescriptor bd, Feature feat) {
        artifact = bd.getArtifact();
        bsn = bd.getBundleSymbolicName();
        version = bd.getArtifact().getId().getOSGiVersion();
        feature = feat;

        Map<String, List<Capability>> caps = new HashMap<>();
        for (Capability c : bd.getCapabilities()) {
            List<Capability> l = caps.get(c.getNamespace());
            if (l == null) {
                l = new ArrayList<>();
                caps.put(c.getNamespace(), l);
            }
            l.add(new CapabilityImpl(this, c));
        }

        // Add the identity capability
        Map<String, Object> idattrs = new HashMap<>();
        idattrs.put(IdentityNamespace.IDENTITY_NAMESPACE, bsn);
        idattrs.put(IdentityNamespace.CAPABILITY_TYPE_ATTRIBUTE, IdentityNamespace.TYPE_BUNDLE);
        idattrs.put(IdentityNamespace.CAPABILITY_VERSION_ATTRIBUTE, version);
        Capability idCap = new CapabilityImpl(this, IdentityNamespace.IDENTITY_NAMESPACE, null, idattrs);
        caps.put(IdentityNamespace.IDENTITY_NAMESPACE, Collections.singletonList(idCap));

        // Add the bundle capability
        Map<String, Object> battrs = new HashMap<>();
        battrs.put(BundleNamespace.BUNDLE_NAMESPACE, bsn);
        battrs.put(BundleNamespace.CAPABILITY_BUNDLE_VERSION_ATTRIBUTE, version);
        Capability bundleCap = new CapabilityImpl(this, BundleNamespace.BUNDLE_NAMESPACE, null, battrs);
        caps.put(BundleNamespace.BUNDLE_NAMESPACE, Collections.singletonList(bundleCap));
        capabilities = Collections.unmodifiableMap(caps);

        Map<String, List<Requirement>> reqs = new HashMap<>();
        for (Requirement r : bd.getRequirements()) {
            List<Requirement> l = reqs.get(r.getNamespace());
            if (l == null) {
                l = new ArrayList<>();
                reqs.put(r.getNamespace(), l);
            }
            // Add the requirement and associate with this resource
            l.add(new RequirementImpl(this, r));
        }

        // TODO What do we do with the execution environment?
        reqs.remove(ExecutionEnvironmentNamespace.EXECUTION_ENVIRONMENT_NAMESPACE);

        requirements = Collections.unmodifiableMap(reqs);
    }

    public BundleResourceImpl(String sn, String ver, Artifact art, Feature feat, Map<String, List<Capability>> caps, Map<String, List<Requirement>> reqs) {
        this(sn, new Version(ver), art, feat, caps, reqs);
    }

    public BundleResourceImpl(String sn, Version ver, Artifact art, Feature feat, Map<String, List<Capability>> caps, Map<String, List<Requirement>> reqs) {
        artifact = art;
        bsn = sn;
        version = ver;
        feature = feat;
        capabilities = caps;
        requirements = reqs;
    }

    @Override
    public Artifact getArtifact() {
        return artifact;
    }

    @Override
    public String getId() {
        return bsn;
    }

    @Override
    public Version getVersion() {
        return version;
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
    public Feature getFeature() {
        return feature;
    }

    @Override
    public boolean equals(Object obj) {
        if ( obj instanceof BundleResourceImpl ) {
            return bsn.equals(((BundleResourceImpl)obj).bsn) && version.equals(((BundleResourceImpl)obj).version);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (bsn + ':' + version).hashCode();

    }

    @Override
    public String toString() {
        return "BundleResourceImpl [bsn=" + bsn + ", version=" + version + "]";
    }
}
