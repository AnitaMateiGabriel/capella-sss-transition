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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.osgi.util.NLS;
import org.polarsys.capella.common.data.modellingcore.AbstractNamedElement;
import org.polarsys.capella.core.data.capellamodeller.SystemEngineering;
import org.polarsys.capella.core.data.cs.AbstractActor;
import org.polarsys.capella.core.data.cs.BlockArchitecture;
import org.polarsys.capella.core.data.cs.CsPackage;
import org.polarsys.capella.core.data.la.LaPackage;
import org.polarsys.capella.core.data.la.LogicalActor;
import org.polarsys.capella.core.data.la.LogicalArchitecture;
import org.polarsys.capella.core.data.pa.PaPackage;
import org.polarsys.capella.core.data.pa.PhysicalActor;
import org.polarsys.capella.core.data.pa.PhysicalComponent;
import org.polarsys.capella.core.data.pa.PhysicalComponentNature;
import org.polarsys.capella.core.model.helpers.BlockArchitectureExt;
import org.polarsys.capella.core.transition.common.constants.ITransitionConstants;
import org.polarsys.capella.core.transition.common.constants.Messages;
import org.polarsys.capella.core.transition.common.handlers.contextscope.ContextScopeHandlerHelper;
import org.polarsys.capella.core.transition.common.handlers.log.LogHelper;
import org.polarsys.capella.core.transition.common.handlers.selection.ISelectionContext;
import org.polarsys.capella.core.transition.common.handlers.selection.SelectionContextHandlerHelper;
import org.polarsys.capella.core.transition.common.handlers.transformation.TransformationHandlerHelper;
import org.polarsys.capella.transition.system2subsystem.crossphases.rules.pa.PhysicalComponentRule;
import org.polarsys.capella.transition.system2subsystem.multiphases.MultiphasesContext;
import org.polarsys.kitalpha.transposer.rules.handler.rules.api.IContext;


public class PhysicalComponentRules {

  public static class ToSA extends PhysicalComponentRule {

  }

  public static class ToLA extends PhysicalComponentRule {

    @Override
    protected EObject getBestContainer(EObject element_p, EObject result_p, IContext context_p) {
      if (((PhysicalComponent) element_p).getDeployingPhysicalComponents().isEmpty()) {
        EObject sourceContainer = element_p.eContainer();
        ISelectionContext sContext =
            SelectionContextHandlerHelper.getHandler(context_p).getSelectionContext(context_p, ITransitionConstants.SELECTION_CONTEXT__TRANSFORMATION,
                element_p, result_p);
        return TransformationHandlerHelper.getInstance(context_p).getBestTracedElement(sourceContainer, context_p, sContext);
      }

      SystemEngineering eng = (SystemEngineering) context_p.get(ITransitionConstants.TRANSFORMATION_TARGET_ROOT);
      LogicalArchitecture la = eng.getContainedLogicalArchitectures().get(0);
      if (result_p instanceof LogicalActor) {
        return la.getOwnedLogicalActorPkg();
      }
      return la.getOwnedLogicalComponent();
    }

    @Override
    protected EObject transformDirectElement(EObject element_p, IContext context_p) {
      PhysicalComponent element = (PhysicalComponent) element_p;
      MultiphasesContext context = (MultiphasesContext) context_p;

      // node pcs in the selection and their children are mapped to the target root logical component
      if (element.getNature() == PhysicalComponentNature.NODE) {
        EObject tmp = element;
        while (tmp instanceof PhysicalComponent) {
          if (context.getSelectedPhysicalComponents().contains(tmp)) {
            return context.getTempRootComponent();
          }
          tmp = tmp.eContainer();
        }
      }

      EObject result = EcoreUtil.create(getTargetType(element_p, context_p));
      if ((element_p instanceof AbstractNamedElement) && (result instanceof AbstractNamedElement)) {
        ((AbstractNamedElement) result).setName(((AbstractNamedElement) element_p).getName());
      }
      return result;
    }

    @Override
    public EClass getTargetType(EObject element_p, IContext context_p) {
      if (ContextScopeHandlerHelper.getInstance(context_p).contains(ITransitionConstants.SOURCE_SCOPE, element_p, context_p)) {
        return LaPackage.Literals.LOGICAL_COMPONENT;
      }
      return LaPackage.Literals.LOGICAL_ACTOR;
    }

    @Override
    protected EStructuralFeature getTargetContainementFeature(EObject element_p, EObject result_p, EObject container_p, IContext context_p) {
      if (result_p instanceof LogicalActor) {
        return LaPackage.Literals.LOGICAL_ACTOR_PKG__OWNED_LOGICAL_ACTORS;
      }
      return LaPackage.Literals.LOGICAL_COMPONENT__OWNED_LOGICAL_COMPONENTS;
    }

    @Override
    public IStatus transformRequired(EObject source_p, IContext context_p) {

      /*
       * Is it in the reference scope?
       * (in LA, sub components in reference scope are transformed, not merged into a parent component)
       */
      if (ContextScopeHandlerHelper.getInstance(context_p).contains(ITransitionConstants.SOURCE_SCOPE, source_p, context_p)) {
        return Status.OK_STATUS;
      }

      IStatus result = super.transformRequired(source_p, context_p);

      return result;
    }

  }

  public static class ToPA extends PhysicalComponentRule {

    @Override
    protected EObject getDefaultContainer(EObject element_p, EObject result_p, IContext context_p) {
      EObject root = TransformationHandlerHelper.getInstance(context_p).getLevelElement(element_p, context_p);
      BlockArchitecture target =
          (BlockArchitecture) TransformationHandlerHelper.getInstance(context_p).getBestTracedElement(root, context_p, CsPackage.Literals.BLOCK_ARCHITECTURE,
              element_p, result_p);

      if (root.equals(element_p.eContainer())) {
        return target;
      }
      if (result_p instanceof AbstractActor) {
        return BlockArchitectureExt.getActorPkg(target, true);
      }
      return BlockArchitectureExt.getFirstComponent(target, true);
    }

    /**
     * @param element_p
     * @param result_p
     * @param context_p
     * @return
     */
    @Override
    protected EObject getBestContainer(EObject element_p, EObject result_p, IContext context_p) {
      EObject bestContainer = null;
      EObject container = getSourceContainer(element_p, result_p, context_p);

      if (container != null) {
        ISelectionContext sContext =
            SelectionContextHandlerHelper.getHandler(context_p).getSelectionContext(context_p, ITransitionConstants.SELECTION_CONTEXT__TRANSFORMATION,
                element_p, result_p);

        bestContainer = TransformationHandlerHelper.getInstance(context_p).getBestTracedElement(container, context_p, sContext);
      }
      return bestContainer;
    }

    @Override
    public EClass getTargetType(EObject element_p, IContext context_p) {
      if (ContextScopeHandlerHelper.getInstance(context_p).contains(ITransitionConstants.SOURCE_SCOPE, element_p, context_p)) {
        return PaPackage.Literals.PHYSICAL_COMPONENT;
      }
      return PaPackage.Literals.PHYSICAL_ACTOR;
    }

    @Override
    protected EStructuralFeature getTargetContainementFeature(EObject element_p, EObject result_p, EObject container_p, IContext context_p) {
      if (result_p instanceof PhysicalActor) {
        return PaPackage.Literals.PHYSICAL_ACTOR_PKG__OWNED_PHYSICAL_ACTORS;
      }
      return PaPackage.Literals.PHYSICAL_COMPONENT__OWNED_PHYSICAL_COMPONENTS;
    }

    @Override
    public IStatus transformRequired(EObject source_p, IContext context_p) {

      IStatus result = super.transformRequired(source_p, context_p);

      if (source_p instanceof PhysicalComponent) {
        if (((PhysicalComponent) source_p).getNature() == PhysicalComponentNature.BEHAVIOR) {
          return new Status(IStatus.WARNING, Messages.Activity_Transformation, NLS.bind("Component ''{0}'' bvehavior ''{1}''",
              LogHelper.getInstance().getText(source_p)));
        }
      }

      /*
       * Is it in the reference scope?
       * (in LA, sub components in reference scope are transformed, not merged into a parent component)
       */
      if (ContextScopeHandlerHelper.getInstance(context_p).contains(ITransitionConstants.SOURCE_SCOPE, source_p, context_p)) {
        return Status.OK_STATUS;
      }

      return result;
    }

  }

}
