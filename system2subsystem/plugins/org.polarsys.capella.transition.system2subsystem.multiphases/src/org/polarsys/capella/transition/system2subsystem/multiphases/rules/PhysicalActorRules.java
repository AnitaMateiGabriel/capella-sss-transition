/*******************************************************************************
 * Copyright (c) 2006, 2015 THALES GLOBAL SERVICES.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *  
 * Contributors:
 *    Thales - initial API and implementation
 *******************************************************************************/
package org.polarsys.capella.transition.system2subsystem.multiphases.rules;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.polarsys.capella.core.data.la.LaPackage;
import org.polarsys.capella.core.data.pa.PaPackage;
import org.polarsys.capella.transition.system2subsystem.crossphases.rules.pa.PhysicalActorRule;
import org.polarsys.kitalpha.transposer.rules.handler.rules.api.IContext;


public class PhysicalActorRules {

  public static class ToSA extends PhysicalActorRule {

  }

  public static class ToLA extends PhysicalActorRule {

    @Override
    public EClass getTargetType(EObject element_p, IContext context_p) {
      return LaPackage.Literals.LOGICAL_ACTOR;
    }

    @Override
    protected EStructuralFeature getTargetContainementFeature(EObject element_p, EObject result_p, EObject container_p, IContext context_p) {
      return LaPackage.Literals.LOGICAL_ACTOR_PKG__OWNED_LOGICAL_ACTORS;
    }

  }

  public static class ToPA extends PhysicalActorRule {
    @Override
    public EClass getTargetType(EObject element_p, IContext context_p) {
      return PaPackage.Literals.PHYSICAL_ACTOR;
    }

    @Override
    protected EStructuralFeature getTargetContainementFeature(EObject element_p, EObject result_p, EObject container_p, IContext context_p) {
      return PaPackage.Literals.PHYSICAL_ACTOR_PKG__OWNED_PHYSICAL_ACTORS;
    }
  }
}
