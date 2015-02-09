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

import java.util.Collection;

import org.eclipse.emf.ecore.EObject;
import org.polarsys.capella.core.data.capellacore.Type;
import org.polarsys.capella.core.data.cs.Component;
import org.polarsys.capella.core.data.cs.Part;
import org.polarsys.capella.core.data.la.LogicalActor;
import org.polarsys.capella.core.data.la.LogicalArchitecture;
import org.polarsys.capella.core.data.pa.PhysicalActor;
import org.polarsys.capella.core.data.pa.PhysicalArchitecture;
import org.polarsys.capella.core.transition.common.constants.ITransitionConstants;
import org.polarsys.capella.core.transition.common.handlers.selection.ISelectionContext;
import org.polarsys.capella.core.transition.common.handlers.selection.SelectionContextHandlerHelper;
import org.polarsys.capella.core.transition.common.handlers.traceability.TraceabilityHandlerHelper;
import org.polarsys.capella.core.transition.common.handlers.transformation.TransformationHandlerHelper;
import org.polarsys.capella.transition.system2subsystem.crossphases.rules.cs.PartRule;
import org.polarsys.capella.transition.system2subsystem.multiphases.MultiphasesContext;
import org.polarsys.kitalpha.transposer.rules.handler.rules.api.IContext;


public class PartRules {

  public static class ToSA extends PartRule {
  }

  public static class ToLA extends PartRule {

    @Override
    protected EObject getBestContainer(EObject element_p, EObject result_p, IContext context_p) {

      LogicalArchitecture la = ((MultiphasesContext) context_p).getTempLogicalArchitecture();

      Type type = ((Part) element_p).getType();
      Collection<EObject> tracedTypes = TraceabilityHandlerHelper.getInstance(context_p).retrieveTracedElements(type, context_p);

      if (tracedTypes.iterator().next() instanceof LogicalActor) {
        return la.getOwnedLogicalContext();
      }

      return TraceabilityHandlerHelper.getInstance(context_p).retrieveTracedElements(type.eContainer(), context_p).iterator().next();

    }

    @Override
    protected EObject transformDirectElement(EObject element_p, IContext context_p) {
      ISelectionContext sContext =
          SelectionContextHandlerHelper.getHandler(context_p).getSelectionContext(context_p, ITransitionConstants.SELECTION_CONTEXT__TRANSFORMATION);
      EObject target = TransformationHandlerHelper.getInstance(context_p).getBestTracedElement(((Part) element_p).getAbstractType(), context_p, sContext);
      Component tComponent = (Component) target;
      if ((tComponent != null) && (tComponent.getRepresentingPartitions() != null) && (tComponent.getRepresentingPartitions().size() > 0)) {
        return tComponent.getRepresentingPartitions().get(0);
      }
      EObject res = super.transformDirectElement(element_p, context_p);
      return res;
    }

  }

  public static class ToPA extends PartRule {

    @Override
    protected EObject getBestContainer(EObject element_p, EObject result_p, IContext context_p) {

      PhysicalArchitecture la = ((MultiphasesContext) context_p).getTempPhysicalArchitecture();

      Type type = ((Part) element_p).getType();
      Collection<EObject> tracedTypes = TraceabilityHandlerHelper.getInstance(context_p).retrieveTracedElements(type, context_p);

      if (tracedTypes.iterator().next() instanceof PhysicalActor) {
        return la.getOwnedPhysicalContext();
      }

      return TraceabilityHandlerHelper.getInstance(context_p).retrieveTracedElements(type.eContainer(), context_p).iterator().next();

    }

    @Override
    protected EObject transformDirectElement(EObject element_p, IContext context_p) {
      ISelectionContext sContext =
          SelectionContextHandlerHelper.getHandler(context_p).getSelectionContext(context_p, ITransitionConstants.SELECTION_CONTEXT__TRANSFORMATION);
      EObject target = TransformationHandlerHelper.getInstance(context_p).getBestTracedElement(((Part) element_p).getAbstractType(), context_p, sContext);
      Component tComponent = (Component) target;
      if ((tComponent != null) && (tComponent.getRepresentingPartitions() != null) && (tComponent.getRepresentingPartitions().size() > 0)) {
        return tComponent.getRepresentingPartitions().get(0);
      }
      EObject res = super.transformDirectElement(element_p, context_p);
      return res;
    }

  }
}
