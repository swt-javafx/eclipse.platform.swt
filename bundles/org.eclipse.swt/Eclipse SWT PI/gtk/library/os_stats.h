/*******************************************************************************
* Copyright (c) 2000, 2005 IBM Corporation and others. All rights reserved.
* The contents of this file are made available under the terms
* of the GNU Lesser General Public License (LGPL) Version 2.1 that
* accompanies this distribution (lgpl-v21.txt).  The LGPL is also
* available at http://www.gnu.org/licenses/lgpl.html.  If the version
* of the LGPL at http://www.gnu.org is different to the version of
* the LGPL accompanying this distribution and there is any conflict
* between the two license versions, the terms of the LGPL accompanying
* this distribution shall govern.
* 
* Contributors:
*     IBM Corporation - initial API and implementation
*******************************************************************************/

#ifdef NATIVE_STATS
extern int OS_nativeFunctionCount;
extern int OS_nativeFunctionCallCount[];
extern char* OS_nativeFunctionNames[];
#define OS_NATIVE_ENTER(env, that, func) OS_nativeFunctionCallCount[func]++;
#define OS_NATIVE_EXIT(env, that, func) 
#else
#define OS_NATIVE_ENTER(env, that, func) 
#define OS_NATIVE_EXIT(env, that, func) 
#endif

typedef enum {
	Call_FUNC,
	GDK_1WINDOWING_1X11_FUNC,
	GInterfaceInfo_1sizeof_FUNC,
	GPollFD_1sizeof_FUNC,
	GTK_1ACCEL_1LABEL_1GET_1ACCEL_1STRING_FUNC,
	GTK_1ACCEL_1LABEL_1SET_1ACCEL_1STRING_FUNC,
	GTK_1ENTRY_1IM_1CONTEXT_FUNC,
	GTK_1SCROLLED_1WINDOW_1HSCROLLBAR_FUNC,
	GTK_1SCROLLED_1WINDOW_1SCROLLBAR_1SPACING_FUNC,
	GTK_1SCROLLED_1WINDOW_1VSCROLLBAR_FUNC,
	GTK_1TEXTVIEW_1IM_1CONTEXT_FUNC,
	GTK_1WIDGET_1HEIGHT_FUNC,
	GTK_1WIDGET_1REQUISITION_1HEIGHT_FUNC,
	GTK_1WIDGET_1REQUISITION_1WIDTH_FUNC,
	GTK_1WIDGET_1SET_1HEIGHT_FUNC,
	GTK_1WIDGET_1SET_1WIDTH_FUNC,
	GTK_1WIDGET_1SET_1X_FUNC,
	GTK_1WIDGET_1SET_1Y_FUNC,
	GTK_1WIDGET_1WIDTH_FUNC,
	GTK_1WIDGET_1WINDOW_FUNC,
	GTK_1WIDGET_1X_FUNC,
	GTK_1WIDGET_1Y_FUNC,
	GTypeInfo_1sizeof_FUNC,
	GTypeQuery_1sizeof_FUNC,
	GdkColor_1sizeof_FUNC,
	GdkDragContext_1sizeof_FUNC,
	GdkEventButton_1sizeof_FUNC,
	GdkEventCrossing_1sizeof_FUNC,
	GdkEventExpose_1sizeof_FUNC,
	GdkEventFocus_1sizeof_FUNC,
	GdkEventKey_1sizeof_FUNC,
	GdkEventMotion_1sizeof_FUNC,
	GdkEventScroll_1sizeof_FUNC,
	GdkEventVisibility_1sizeof_FUNC,
	GdkEventWindowState_1sizeof_FUNC,
	GdkEvent_1sizeof_FUNC,
	GdkGCValues_1sizeof_FUNC,
	GdkGeometry_1sizeof_FUNC,
	GdkImage_1sizeof_FUNC,
	GdkRectangle_1sizeof_FUNC,
	GdkVisual_1sizeof_FUNC,
	GdkWindowAttr_1sizeof_FUNC,
	GtkAdjustment_1sizeof_FUNC,
	GtkAllocation_1sizeof_FUNC,
	GtkBorder_1sizeof_FUNC,
	GtkColorSelectionDialog_1sizeof_FUNC,
	GtkCombo_1sizeof_FUNC,
	GtkFileSelection_1sizeof_FUNC,
	GtkFixedClass_1sizeof_FUNC,
	GtkFixed_1sizeof_FUNC,
	GtkRequisition_1sizeof_FUNC,
	GtkSelectionData_1sizeof_FUNC,
	GtkTargetEntry_1sizeof_FUNC,
	GtkTargetPair_1sizeof_FUNC,
	GtkTextIter_1sizeof_FUNC,
	GtkTreeIter_1sizeof_FUNC,
	PTR_1sizeof_FUNC,
	PangoAttribute_1sizeof_FUNC,
	PangoItem_1sizeof_FUNC,
	PangoLayoutLine_1sizeof_FUNC,
	PangoLayoutRun_1sizeof_FUNC,
	PangoLogAttr_1sizeof_FUNC,
	PangoRectangle_1sizeof_FUNC,
	XAnyEvent_1sizeof_FUNC,
	XButtonEvent_1sizeof_FUNC,
	XClientMessageEvent_1sizeof_FUNC,
	XCrossingEvent_1sizeof_FUNC,
	XEvent_1sizeof_FUNC,
	XExposeEvent_1sizeof_FUNC,
	XFocusChangeEvent_1sizeof_FUNC,
	XVisibilityEvent_1sizeof_FUNC,
	XWindowChanges_1sizeof_FUNC,
	_1Call_FUNC,
	_1GDK_1DISPLAY_FUNC,
	_1GDK_1PIXMAP_1XID_FUNC,
	_1GDK_1ROOT_1PARENT_FUNC,
	_1GDK_1TYPE_1COLOR_FUNC,
	_1GDK_1TYPE_1PIXBUF_FUNC,
	_1GTK_1IS_1BUTTON_FUNC,
	_1GTK_1IS_1CELL_1RENDERER_1PIXBUF_FUNC,
	_1GTK_1IS_1CELL_1RENDERER_1TEXT_FUNC,
	_1GTK_1IS_1CONTAINER_FUNC,
	_1GTK_1IS_1IMAGE_1MENU_1ITEM_FUNC,
	_1GTK_1STOCK_1CANCEL_FUNC,
	_1GTK_1STOCK_1OK_FUNC,
	_1GTK_1TYPE_1FIXED_FUNC,
	_1GTK_1WIDGET_1FLAGS_FUNC,
	_1GTK_1WIDGET_1HAS_1DEFAULT_FUNC,
	_1GTK_1WIDGET_1HAS_1FOCUS_FUNC,
	_1GTK_1WIDGET_1IS_1SENSITIVE_FUNC,
	_1GTK_1WIDGET_1MAPPED_FUNC,
	_1GTK_1WIDGET_1SENSITIVE_FUNC,
	_1GTK_1WIDGET_1SET_1FLAGS_FUNC,
	_1GTK_1WIDGET_1STATE_FUNC,
	_1GTK_1WIDGET_1UNSET_1FLAGS_FUNC,
	_1GTK_1WIDGET_1VISIBLE_FUNC,
	_1G_1OBJECT_1CLASS_FUNC,
	_1G_1OBJECT_1GET_1CLASS_FUNC,
	_1G_1OBJECT_1TYPE_FUNC,
	_1G_1TYPE_1BOOLEAN_FUNC,
	_1G_1TYPE_1INT_FUNC,
	_1G_1TYPE_1STRING_FUNC,
	_1PANGO_1PIXELS_FUNC,
	_1PANGO_1TYPE_1FONT_1DESCRIPTION_FUNC,
	_1XCheckIfEvent_FUNC,
	_1XCheckMaskEvent_FUNC,
	_1XCheckWindowEvent_FUNC,
	_1XDefaultRootWindow_FUNC,
	_1XDefaultScreen_FUNC,
	_1XFlush_FUNC,
	_1XFree_FUNC,
	_1XGetSelectionOwner_FUNC,
	_1XInternAtom_FUNC,
	_1XKeysymToKeycode_FUNC,
	_1XListProperties_FUNC,
	_1XQueryTree_FUNC,
	_1XReconfigureWMWindow_FUNC,
	_1XSendEvent_FUNC,
	_1XSetErrorHandler_FUNC,
	_1XSetIOErrorHandler_FUNC,
	_1XSetInputFocus_FUNC,
	_1XSynchronize_FUNC,
	_1XTestFakeButtonEvent_FUNC,
	_1XTestFakeKeyEvent_FUNC,
	_1XTestFakeMotionEvent_FUNC,
	_1XWarpPointer_FUNC,
	_1g_1filename_1from_1uri_FUNC,
	_1g_1filename_1from_1utf8_FUNC,
	_1g_1filename_1to_1uri_FUNC,
	_1g_1filename_1to_1utf8_FUNC,
	_1g_1free_FUNC,
	_1g_1list_1append_FUNC,
	_1g_1list_1data_FUNC,
	_1g_1list_1free_FUNC,
	_1g_1list_1free_11_FUNC,
	_1g_1list_1length_FUNC,
	_1g_1list_1next_FUNC,
	_1g_1list_1nth_FUNC,
	_1g_1list_1nth_1data_FUNC,
	_1g_1list_1prepend_FUNC,
	_1g_1list_1previous_FUNC,
	_1g_1list_1remove_1link_FUNC,
	_1g_1list_1reverse_FUNC,
	_1g_1list_1set_1next_FUNC,
	_1g_1list_1set_1previous_FUNC,
	_1g_1locale_1from_1utf8_FUNC,
	_1g_1locale_1to_1utf8_FUNC,
	_1g_1log_1default_1handler_FUNC,
	_1g_1log_1remove_1handler_FUNC,
	_1g_1log_1set_1handler_FUNC,
	_1g_1main_1context_1acquire_FUNC,
	_1g_1main_1context_1check_FUNC,
	_1g_1main_1context_1default_FUNC,
	_1g_1main_1context_1get_1poll_1func_FUNC,
	_1g_1main_1context_1iteration_FUNC,
	_1g_1main_1context_1pending_FUNC,
	_1g_1main_1context_1prepare_FUNC,
	_1g_1main_1context_1query_FUNC,
	_1g_1main_1context_1release_FUNC,
	_1g_1malloc_FUNC,
	_1g_1object_1get_FUNC,
	_1g_1object_1get_1qdata_FUNC,
	_1g_1object_1new_FUNC,
	_1g_1object_1ref_FUNC,
	_1g_1object_1set__I_3BFI_FUNC,
	_1g_1object_1set__I_3BII_FUNC,
	_1g_1object_1set__I_3BJI_FUNC,
	_1g_1object_1set__I_3BZI_FUNC,
	_1g_1object_1set_1qdata_FUNC,
	_1g_1object_1unref_FUNC,
	_1g_1quark_1from_1string_FUNC,
	_1g_1signal_1connect_FUNC,
	_1g_1signal_1connect_1after_FUNC,
	_1g_1signal_1emit_1by_1name__I_3B_FUNC,
	_1g_1signal_1emit_1by_1name__I_3BI_FUNC,
	_1g_1signal_1emit_1by_1name__I_3BII_FUNC,
	_1g_1signal_1emit_1by_1name__I_3B_3B_FUNC,
	_1g_1signal_1handler_1disconnect_FUNC,
	_1g_1signal_1handlers_1block_1matched_FUNC,
	_1g_1signal_1handlers_1disconnect_1matched_FUNC,
	_1g_1signal_1handlers_1unblock_1matched_FUNC,
	_1g_1signal_1lookup_FUNC,
	_1g_1signal_1stop_1emission_1by_1name_FUNC,
	_1g_1slist_1data_FUNC,
	_1g_1slist_1free_FUNC,
	_1g_1slist_1length_FUNC,
	_1g_1slist_1next_FUNC,
	_1g_1strfreev_FUNC,
	_1g_1thread_1init_FUNC,
	_1g_1thread_1supported_FUNC,
	_1g_1type_1add_1interface_1static_FUNC,
	_1g_1type_1class_1peek_FUNC,
	_1g_1type_1class_1peek_1parent_FUNC,
	_1g_1type_1from_1name_FUNC,
	_1g_1type_1interface_1peek_1parent_FUNC,
	_1g_1type_1is_1a_FUNC,
	_1g_1type_1name_FUNC,
	_1g_1type_1parent_FUNC,
	_1g_1type_1query_FUNC,
	_1g_1type_1register_1static_FUNC,
	_1g_1utf16_1to_1utf8_FUNC,
	_1g_1utf8_1offset_1to_1pointer_FUNC,
	_1g_1utf8_1pointer_1to_1offset_FUNC,
	_1g_1utf8_1strlen_FUNC,
	_1g_1utf8_1to_1utf16__II_3I_3I_3I_FUNC,
	_1g_1utf8_1to_1utf16___3BI_3I_3I_3I_FUNC,
	_1gdk_1atom_1intern_FUNC,
	_1gdk_1atom_1name_FUNC,
	_1gdk_1beep_FUNC,
	_1gdk_1bitmap_1create_1from_1data_FUNC,
	_1gdk_1color_1white_FUNC,
	_1gdk_1colormap_1alloc_1color_FUNC,
	_1gdk_1colormap_1free_1colors_FUNC,
	_1gdk_1colormap_1get_1system_FUNC,
	_1gdk_1colormap_1query_1color_FUNC,
	_1gdk_1cursor_1destroy_FUNC,
	_1gdk_1cursor_1new_FUNC,
	_1gdk_1cursor_1new_1from_1pixmap_FUNC,
	_1gdk_1drag_1status_FUNC,
	_1gdk_1draw_1arc_FUNC,
	_1gdk_1draw_1drawable_FUNC,
	_1gdk_1draw_1layout_FUNC,
	_1gdk_1draw_1layout_1with_1colors_FUNC,
	_1gdk_1draw_1line_FUNC,
	_1gdk_1draw_1lines_FUNC,
	_1gdk_1draw_1pixbuf_FUNC,
	_1gdk_1draw_1point_FUNC,
	_1gdk_1draw_1polygon_FUNC,
	_1gdk_1draw_1rectangle_FUNC,
	_1gdk_1drawable_1get_1image_FUNC,
	_1gdk_1drawable_1get_1size_FUNC,
	_1gdk_1drawable_1get_1visible_1region_FUNC,
	_1gdk_1error_1trap_1pop_FUNC,
	_1gdk_1error_1trap_1push_FUNC,
	_1gdk_1event_1copy_FUNC,
	_1gdk_1event_1free_FUNC,
	_1gdk_1event_1get_FUNC,
	_1gdk_1event_1get_1coords_FUNC,
	_1gdk_1event_1get_1graphics_1expose_FUNC,
	_1gdk_1event_1get_1root_1coords_FUNC,
	_1gdk_1event_1get_1state_FUNC,
	_1gdk_1event_1get_1time_FUNC,
	_1gdk_1event_1handler_1set_FUNC,
	_1gdk_1event_1put_FUNC,
	_1gdk_1flush_FUNC,
	_1gdk_1free_1text_1list_FUNC,
	_1gdk_1gc_1get_1values_FUNC,
	_1gdk_1gc_1new_FUNC,
	_1gdk_1gc_1set_1background_FUNC,
	_1gdk_1gc_1set_1clip_1mask_FUNC,
	_1gdk_1gc_1set_1clip_1origin_FUNC,
	_1gdk_1gc_1set_1clip_1rectangle_FUNC,
	_1gdk_1gc_1set_1clip_1region_FUNC,
	_1gdk_1gc_1set_1dashes_FUNC,
	_1gdk_1gc_1set_1exposures_FUNC,
	_1gdk_1gc_1set_1fill_FUNC,
	_1gdk_1gc_1set_1foreground_FUNC,
	_1gdk_1gc_1set_1function_FUNC,
	_1gdk_1gc_1set_1line_1attributes_FUNC,
	_1gdk_1gc_1set_1stipple_FUNC,
	_1gdk_1gc_1set_1subwindow_FUNC,
	_1gdk_1gc_1set_1values_FUNC,
	_1gdk_1image_1get_FUNC,
	_1gdk_1image_1get_1pixel_FUNC,
	_1gdk_1keyboard_1ungrab_FUNC,
	_1gdk_1keymap_1get_1default_FUNC,
	_1gdk_1keymap_1translate_1keyboard_1state_FUNC,
	_1gdk_1keyval_1to_1lower_FUNC,
	_1gdk_1keyval_1to_1unicode_FUNC,
	_1gdk_1pango_1context_1get_FUNC,
	_1gdk_1pango_1context_1set_1colormap_FUNC,
	_1gdk_1pango_1layout_1get_1clip_1region_FUNC,
	_1gdk_1pixbuf_1get_1from_1drawable_FUNC,
	_1gdk_1pixbuf_1get_1has_1alpha_FUNC,
	_1gdk_1pixbuf_1get_1height_FUNC,
	_1gdk_1pixbuf_1get_1pixels_FUNC,
	_1gdk_1pixbuf_1get_1rowstride_FUNC,
	_1gdk_1pixbuf_1get_1width_FUNC,
	_1gdk_1pixbuf_1new_FUNC,
	_1gdk_1pixbuf_1render_1pixmap_1and_1mask_FUNC,
	_1gdk_1pixbuf_1render_1to_1drawable_FUNC,
	_1gdk_1pixbuf_1render_1to_1drawable_1alpha_FUNC,
	_1gdk_1pixbuf_1scale_FUNC,
	_1gdk_1pixbuf_1scale_1simple_FUNC,
	_1gdk_1pixmap_1foreign_1new_FUNC,
	_1gdk_1pixmap_1new_FUNC,
	_1gdk_1pointer_1grab_FUNC,
	_1gdk_1pointer_1is_1grabbed_FUNC,
	_1gdk_1pointer_1ungrab_FUNC,
	_1gdk_1property_1get_FUNC,
	_1gdk_1region_1destroy_FUNC,
	_1gdk_1region_1empty_FUNC,
	_1gdk_1region_1get_1clipbox_FUNC,
	_1gdk_1region_1get_1rectangles_FUNC,
	_1gdk_1region_1intersect_FUNC,
	_1gdk_1region_1new_FUNC,
	_1gdk_1region_1offset_FUNC,
	_1gdk_1region_1point_1in_FUNC,
	_1gdk_1region_1polygon_FUNC,
	_1gdk_1region_1rect_1in_FUNC,
	_1gdk_1region_1rectangle_FUNC,
	_1gdk_1region_1subtract_FUNC,
	_1gdk_1region_1union_FUNC,
	_1gdk_1region_1union_1with_1rect_FUNC,
	_1gdk_1rgb_1init_FUNC,
	_1gdk_1screen_1get_1default_FUNC,
	_1gdk_1screen_1get_1monitor_1at_1window_FUNC,
	_1gdk_1screen_1get_1monitor_1geometry_FUNC,
	_1gdk_1screen_1get_1n_1monitors_FUNC,
	_1gdk_1screen_1get_1number_FUNC,
	_1gdk_1screen_1height_FUNC,
	_1gdk_1screen_1width_FUNC,
	_1gdk_1screen_1width_1mm_FUNC,
	_1gdk_1set_1program_1class_FUNC,
	_1gdk_1text_1property_1to_1utf8_1list_FUNC,
	_1gdk_1unicode_1to_1keyval_FUNC,
	_1gdk_1utf8_1to_1compound_1text_FUNC,
	_1gdk_1visual_1get_1system_FUNC,
	_1gdk_1window_1add_1filter_FUNC,
	_1gdk_1window_1at_1pointer_FUNC,
	_1gdk_1window_1begin_1paint_1rect_FUNC,
	_1gdk_1window_1destroy_FUNC,
	_1gdk_1window_1end_1paint_FUNC,
	_1gdk_1window_1focus_FUNC,
	_1gdk_1window_1freeze_1updates_FUNC,
	_1gdk_1window_1get_1children_FUNC,
	_1gdk_1window_1get_1events_FUNC,
	_1gdk_1window_1get_1frame_1extents_FUNC,
	_1gdk_1window_1get_1internal_1paint_1info_FUNC,
	_1gdk_1window_1get_1origin_FUNC,
	_1gdk_1window_1get_1parent_FUNC,
	_1gdk_1window_1get_1pointer_FUNC,
	_1gdk_1window_1get_1user_1data_FUNC,
	_1gdk_1window_1hide_FUNC,
	_1gdk_1window_1invalidate_1rect_FUNC,
	_1gdk_1window_1invalidate_1region_FUNC,
	_1gdk_1window_1lookup_FUNC,
	_1gdk_1window_1lower_FUNC,
	_1gdk_1window_1move_FUNC,
	_1gdk_1window_1new_FUNC,
	_1gdk_1window_1process_1all_1updates_FUNC,
	_1gdk_1window_1process_1updates_FUNC,
	_1gdk_1window_1raise_FUNC,
	_1gdk_1window_1remove_1filter_FUNC,
	_1gdk_1window_1resize_FUNC,
	_1gdk_1window_1scroll_FUNC,
	_1gdk_1window_1set_1accept_1focus_FUNC,
	_1gdk_1window_1set_1back_1pixmap_FUNC,
	_1gdk_1window_1set_1cursor_FUNC,
	_1gdk_1window_1set_1decorations_FUNC,
	_1gdk_1window_1set_1events_FUNC,
	_1gdk_1window_1set_1icon_FUNC,
	_1gdk_1window_1set_1icon_1list_FUNC,
	_1gdk_1window_1set_1keep_1above_FUNC,
	_1gdk_1window_1set_1override_1redirect_FUNC,
	_1gdk_1window_1set_1user_1data_FUNC,
	_1gdk_1window_1shape_1combine_1region_FUNC,
	_1gdk_1window_1show_FUNC,
	_1gdk_1window_1show_1unraised_FUNC,
	_1gdk_1window_1thaw_1updates_FUNC,
	_1gdk_1x11_1atom_1to_1xatom_FUNC,
	_1gdk_1x11_1colormap_1get_1xcolormap_FUNC,
	_1gdk_1x11_1drawable_1get_1xdisplay_FUNC,
	_1gdk_1x11_1drawable_1get_1xid_FUNC,
	_1gdk_1x11_1visual_1get_1xvisual_FUNC,
	_1gtk_1accel_1group_1new_FUNC,
	_1gtk_1accel_1groups_1activate_FUNC,
	_1gtk_1accel_1label_1set_1accel_1widget_FUNC,
	_1gtk_1adjustment_1changed_FUNC,
	_1gtk_1adjustment_1new_FUNC,
	_1gtk_1adjustment_1set_1value_FUNC,
	_1gtk_1adjustment_1value_1changed_FUNC,
	_1gtk_1arrow_1new_FUNC,
	_1gtk_1arrow_1set_FUNC,
	_1gtk_1bin_1get_1child_FUNC,
	_1gtk_1box_1set_1child_1packing_FUNC,
	_1gtk_1button_1clicked_FUNC,
	_1gtk_1button_1new_FUNC,
	_1gtk_1button_1set_1relief_FUNC,
	_1gtk_1cell_1renderer_1get_1size_FUNC,
	_1gtk_1cell_1renderer_1pixbuf_1new_FUNC,
	_1gtk_1cell_1renderer_1text_1new_FUNC,
	_1gtk_1cell_1renderer_1toggle_1new_FUNC,
	_1gtk_1check_1button_1new_FUNC,
	_1gtk_1check_1menu_1item_1get_1active_FUNC,
	_1gtk_1check_1menu_1item_1new_1with_1label_FUNC,
	_1gtk_1check_1menu_1item_1set_1active_FUNC,
	_1gtk_1check_1version_FUNC,
	_1gtk_1clipboard_1clear_FUNC,
	_1gtk_1clipboard_1get_FUNC,
	_1gtk_1clipboard_1set_1with_1data_FUNC,
	_1gtk_1clipboard_1wait_1for_1contents_FUNC,
	_1gtk_1color_1selection_1dialog_1new_FUNC,
	_1gtk_1color_1selection_1get_1current_1color_FUNC,
	_1gtk_1color_1selection_1set_1current_1color_FUNC,
	_1gtk_1color_1selection_1set_1has_1palette_FUNC,
	_1gtk_1combo_1disable_1activate_FUNC,
	_1gtk_1combo_1new_FUNC,
	_1gtk_1combo_1set_1case_1sensitive_FUNC,
	_1gtk_1combo_1set_1popdown_1strings_FUNC,
	_1gtk_1container_1add_FUNC,
	_1gtk_1container_1forall_FUNC,
	_1gtk_1container_1get_1border_1width_FUNC,
	_1gtk_1container_1get_1children_FUNC,
	_1gtk_1container_1remove_FUNC,
	_1gtk_1container_1resize_1children_FUNC,
	_1gtk_1container_1set_1border_1width_FUNC,
	_1gtk_1dialog_1add_1button_FUNC,
	_1gtk_1dialog_1run_FUNC,
	_1gtk_1drag_1begin_FUNC,
	_1gtk_1drag_1check_1threshold_FUNC,
	_1gtk_1drag_1dest_1find_1target_FUNC,
	_1gtk_1drag_1dest_1set_FUNC,
	_1gtk_1drag_1dest_1unset_FUNC,
	_1gtk_1drag_1finish_FUNC,
	_1gtk_1drag_1get_1data_FUNC,
	_1gtk_1drawing_1area_1new_FUNC,
	_1gtk_1editable_1copy_1clipboard_FUNC,
	_1gtk_1editable_1cut_1clipboard_FUNC,
	_1gtk_1editable_1delete_1selection_FUNC,
	_1gtk_1editable_1delete_1text_FUNC,
	_1gtk_1editable_1get_1chars_FUNC,
	_1gtk_1editable_1get_1editable_FUNC,
	_1gtk_1editable_1get_1position_FUNC,
	_1gtk_1editable_1get_1selection_1bounds_FUNC,
	_1gtk_1editable_1insert_1text_FUNC,
	_1gtk_1editable_1paste_1clipboard_FUNC,
	_1gtk_1editable_1select_1region_FUNC,
	_1gtk_1editable_1set_1editable_FUNC,
	_1gtk_1editable_1set_1position_FUNC,
	_1gtk_1entry_1get_1invisible_1char_FUNC,
	_1gtk_1entry_1get_1layout_FUNC,
	_1gtk_1entry_1get_1max_1length_FUNC,
	_1gtk_1entry_1get_1text_FUNC,
	_1gtk_1entry_1get_1visibility_FUNC,
	_1gtk_1entry_1new_FUNC,
	_1gtk_1entry_1set_1activates_1default_FUNC,
	_1gtk_1entry_1set_1alignment_FUNC,
	_1gtk_1entry_1set_1has_1frame_FUNC,
	_1gtk_1entry_1set_1invisible_1char_FUNC,
	_1gtk_1entry_1set_1max_1length_FUNC,
	_1gtk_1entry_1set_1text_FUNC,
	_1gtk_1entry_1set_1visibility_FUNC,
	_1gtk_1events_1pending_FUNC,
	_1gtk_1file_1chooser_1add_1filter_FUNC,
	_1gtk_1file_1chooser_1dialog_1new_FUNC,
	_1gtk_1file_1chooser_1get_1current_1folder_FUNC,
	_1gtk_1file_1chooser_1get_1filename_FUNC,
	_1gtk_1file_1chooser_1get_1filenames_FUNC,
	_1gtk_1file_1chooser_1set_1current_1folder_FUNC,
	_1gtk_1file_1chooser_1set_1current_1name_FUNC,
	_1gtk_1file_1chooser_1set_1extra_1widget_FUNC,
	_1gtk_1file_1chooser_1set_1filename_FUNC,
	_1gtk_1file_1chooser_1set_1select_1multiple_FUNC,
	_1gtk_1file_1filter_1add_1pattern_FUNC,
	_1gtk_1file_1filter_1new_FUNC,
	_1gtk_1file_1filter_1set_1name_FUNC,
	_1gtk_1file_1selection_1get_1filename_FUNC,
	_1gtk_1file_1selection_1get_1selections_FUNC,
	_1gtk_1file_1selection_1hide_1fileop_1buttons_FUNC,
	_1gtk_1file_1selection_1new_FUNC,
	_1gtk_1file_1selection_1set_1filename_FUNC,
	_1gtk_1file_1selection_1set_1select_1multiple_FUNC,
	_1gtk_1fixed_1move_FUNC,
	_1gtk_1fixed_1new_FUNC,
	_1gtk_1fixed_1set_1has_1window_FUNC,
	_1gtk_1font_1selection_1dialog_1get_1font_1name_FUNC,
	_1gtk_1font_1selection_1dialog_1new_FUNC,
	_1gtk_1font_1selection_1dialog_1set_1font_1name_FUNC,
	_1gtk_1frame_1get_1label_1widget_FUNC,
	_1gtk_1frame_1new_FUNC,
	_1gtk_1frame_1set_1label_FUNC,
	_1gtk_1frame_1set_1label_1widget_FUNC,
	_1gtk_1frame_1set_1shadow_1type_FUNC,
	_1gtk_1get_1current_1event_FUNC,
	_1gtk_1get_1current_1event_1state_FUNC,
	_1gtk_1get_1current_1event_1time_FUNC,
	_1gtk_1get_1default_1language_FUNC,
	_1gtk_1get_1event_1widget_FUNC,
	_1gtk_1grab_1add_FUNC,
	_1gtk_1grab_1get_1current_FUNC,
	_1gtk_1grab_1remove_FUNC,
	_1gtk_1hbox_1new_FUNC,
	_1gtk_1hscale_1new_FUNC,
	_1gtk_1hscrollbar_1new_FUNC,
	_1gtk_1hseparator_1new_FUNC,
	_1gtk_1icon_1factory_1lookup_1default_FUNC,
	_1gtk_1icon_1set_1render_1icon_FUNC,
	_1gtk_1im_1context_1filter_1keypress_FUNC,
	_1gtk_1im_1context_1focus_1in_FUNC,
	_1gtk_1im_1context_1focus_1out_FUNC,
	_1gtk_1im_1context_1get_1preedit_1string_FUNC,
	_1gtk_1im_1context_1get_1type_FUNC,
	_1gtk_1im_1context_1reset_FUNC,
	_1gtk_1im_1context_1set_1client_1window_FUNC,
	_1gtk_1im_1context_1set_1cursor_1location_FUNC,
	_1gtk_1im_1multicontext_1append_1menuitems_FUNC,
	_1gtk_1im_1multicontext_1new_FUNC,
	_1gtk_1image_1menu_1item_1new_1with_1label_FUNC,
	_1gtk_1image_1menu_1item_1set_1image_FUNC,
	_1gtk_1image_1new_FUNC,
	_1gtk_1image_1new_1from_1pixbuf_FUNC,
	_1gtk_1image_1new_1from_1pixmap_FUNC,
	_1gtk_1image_1set_1from_1pixbuf_FUNC,
	_1gtk_1image_1set_1from_1pixmap_FUNC,
	_1gtk_1init_1check_FUNC,
	_1gtk_1label_1get_1mnemonic_1keyval_FUNC,
	_1gtk_1label_1new_FUNC,
	_1gtk_1label_1new_1with_1mnemonic_FUNC,
	_1gtk_1label_1set_1attributes_FUNC,
	_1gtk_1label_1set_1justify_FUNC,
	_1gtk_1label_1set_1line_1wrap_FUNC,
	_1gtk_1label_1set_1text__II_FUNC,
	_1gtk_1label_1set_1text__I_3B_FUNC,
	_1gtk_1label_1set_1text_1with_1mnemonic_FUNC,
	_1gtk_1list_1append_1items_FUNC,
	_1gtk_1list_1clear_1items_FUNC,
	_1gtk_1list_1insert_1items_FUNC,
	_1gtk_1list_1item_1new_1with_1label_FUNC,
	_1gtk_1list_1remove_1items_FUNC,
	_1gtk_1list_1select_1item_FUNC,
	_1gtk_1list_1store_1append_FUNC,
	_1gtk_1list_1store_1clear_FUNC,
	_1gtk_1list_1store_1insert_FUNC,
	_1gtk_1list_1store_1newv_FUNC,
	_1gtk_1list_1store_1remove_FUNC,
	_1gtk_1list_1store_1set__IIIII_FUNC,
	_1gtk_1list_1store_1set__IIIJI_FUNC,
	_1gtk_1list_1store_1set__IIILorg_eclipse_swt_internal_gtk_GdkColor_2I_FUNC,
	_1gtk_1list_1store_1set__IIIZI_FUNC,
	_1gtk_1list_1store_1set__III_3BI_FUNC,
	_1gtk_1list_1unselect_1all_FUNC,
	_1gtk_1list_1unselect_1item_FUNC,
	_1gtk_1main_FUNC,
	_1gtk_1main_1do_1event_FUNC,
	_1gtk_1main_1iteration_FUNC,
	_1gtk_1major_1version_FUNC,
	_1gtk_1menu_1bar_1new_FUNC,
	_1gtk_1menu_1item_1remove_1submenu_FUNC,
	_1gtk_1menu_1item_1set_1submenu_FUNC,
	_1gtk_1menu_1new_FUNC,
	_1gtk_1menu_1popdown_FUNC,
	_1gtk_1menu_1popup_FUNC,
	_1gtk_1menu_1shell_1deactivate_FUNC,
	_1gtk_1menu_1shell_1insert_FUNC,
	_1gtk_1menu_1shell_1select_1item_FUNC,
	_1gtk_1message_1dialog_1new_FUNC,
	_1gtk_1micro_1version_FUNC,
	_1gtk_1minor_1version_FUNC,
	_1gtk_1misc_1set_1alignment_FUNC,
	_1gtk_1notebook_1get_1current_1page_FUNC,
	_1gtk_1notebook_1get_1scrollable_FUNC,
	_1gtk_1notebook_1insert_1page_FUNC,
	_1gtk_1notebook_1new_FUNC,
	_1gtk_1notebook_1remove_1page_FUNC,
	_1gtk_1notebook_1set_1current_1page_FUNC,
	_1gtk_1notebook_1set_1scrollable_FUNC,
	_1gtk_1notebook_1set_1show_1tabs_FUNC,
	_1gtk_1notebook_1set_1tab_1pos_FUNC,
	_1gtk_1object_1sink_FUNC,
	_1gtk_1paint_1focus_FUNC,
	_1gtk_1paint_1handle_FUNC,
	_1gtk_1plug_1get_1id_FUNC,
	_1gtk_1plug_1new_FUNC,
	_1gtk_1progress_1bar_1new_FUNC,
	_1gtk_1progress_1bar_1pulse_FUNC,
	_1gtk_1progress_1bar_1set_1fraction_FUNC,
	_1gtk_1progress_1bar_1set_1orientation_FUNC,
	_1gtk_1radio_1button_1get_1group_FUNC,
	_1gtk_1radio_1button_1new_FUNC,
	_1gtk_1radio_1menu_1item_1get_1group_FUNC,
	_1gtk_1radio_1menu_1item_1new_FUNC,
	_1gtk_1radio_1menu_1item_1new_1with_1label_FUNC,
	_1gtk_1range_1get_1adjustment_FUNC,
	_1gtk_1range_1set_1increments_FUNC,
	_1gtk_1range_1set_1inverted_FUNC,
	_1gtk_1range_1set_1range_FUNC,
	_1gtk_1range_1set_1value_FUNC,
	_1gtk_1rc_1parse_1string_FUNC,
	_1gtk_1rc_1style_1get_1bg_1pixmap_1name_FUNC,
	_1gtk_1rc_1style_1get_1color_1flags_FUNC,
	_1gtk_1rc_1style_1set_1bg_FUNC,
	_1gtk_1rc_1style_1set_1bg_1pixmap_1name_FUNC,
	_1gtk_1rc_1style_1set_1color_1flags_FUNC,
	_1gtk_1scale_1set_1digits_FUNC,
	_1gtk_1scale_1set_1draw_1value_FUNC,
	_1gtk_1scrolled_1window_1get_1hadjustment_FUNC,
	_1gtk_1scrolled_1window_1get_1policy_FUNC,
	_1gtk_1scrolled_1window_1get_1shadow_1type_FUNC,
	_1gtk_1scrolled_1window_1get_1vadjustment_FUNC,
	_1gtk_1scrolled_1window_1new_FUNC,
	_1gtk_1scrolled_1window_1set_1placement_FUNC,
	_1gtk_1scrolled_1window_1set_1policy_FUNC,
	_1gtk_1scrolled_1window_1set_1shadow_1type_FUNC,
	_1gtk_1selection_1data_1free_FUNC,
	_1gtk_1selection_1data_1set_FUNC,
	_1gtk_1separator_1menu_1item_1new_FUNC,
	_1gtk_1set_1locale_FUNC,
	_1gtk_1settings_1get_1default_FUNC,
	_1gtk_1socket_1get_1id_FUNC,
	_1gtk_1socket_1new_FUNC,
	_1gtk_1spin_1button_1get_1adjustment_FUNC,
	_1gtk_1spin_1button_1get_1digits_FUNC,
	_1gtk_1spin_1button_1new_FUNC,
	_1gtk_1spin_1button_1set_1digits_FUNC,
	_1gtk_1spin_1button_1set_1increments_FUNC,
	_1gtk_1spin_1button_1set_1range_FUNC,
	_1gtk_1spin_1button_1set_1value_FUNC,
	_1gtk_1spin_1button_1set_1wrap_FUNC,
	_1gtk_1style_1get_1base_FUNC,
	_1gtk_1style_1get_1bg_FUNC,
	_1gtk_1style_1get_1black_FUNC,
	_1gtk_1style_1get_1dark_FUNC,
	_1gtk_1style_1get_1fg_FUNC,
	_1gtk_1style_1get_1font_1desc_FUNC,
	_1gtk_1style_1get_1light_FUNC,
	_1gtk_1style_1get_1text_FUNC,
	_1gtk_1style_1get_1xthickness_FUNC,
	_1gtk_1style_1get_1ythickness_FUNC,
	_1gtk_1target_1list_1new_FUNC,
	_1gtk_1target_1list_1unref_FUNC,
	_1gtk_1text_1buffer_1copy_1clipboard_FUNC,
	_1gtk_1text_1buffer_1cut_1clipboard_FUNC,
	_1gtk_1text_1buffer_1delete_FUNC,
	_1gtk_1text_1buffer_1get_1bounds_FUNC,
	_1gtk_1text_1buffer_1get_1char_1count_FUNC,
	_1gtk_1text_1buffer_1get_1end_1iter_FUNC,
	_1gtk_1text_1buffer_1get_1insert_FUNC,
	_1gtk_1text_1buffer_1get_1iter_1at_1line_FUNC,
	_1gtk_1text_1buffer_1get_1iter_1at_1mark_FUNC,
	_1gtk_1text_1buffer_1get_1iter_1at_1offset_FUNC,
	_1gtk_1text_1buffer_1get_1line_1count_FUNC,
	_1gtk_1text_1buffer_1get_1selection_1bound_FUNC,
	_1gtk_1text_1buffer_1get_1selection_1bounds_FUNC,
	_1gtk_1text_1buffer_1get_1text_FUNC,
	_1gtk_1text_1buffer_1insert__II_3BI_FUNC,
	_1gtk_1text_1buffer_1insert__I_3B_3BI_FUNC,
	_1gtk_1text_1buffer_1move_1mark_FUNC,
	_1gtk_1text_1buffer_1paste_1clipboard_FUNC,
	_1gtk_1text_1buffer_1place_1cursor_FUNC,
	_1gtk_1text_1buffer_1set_1text_FUNC,
	_1gtk_1text_1iter_1get_1line_FUNC,
	_1gtk_1text_1iter_1get_1offset_FUNC,
	_1gtk_1text_1view_1buffer_1to_1window_1coords_FUNC,
	_1gtk_1text_1view_1get_1buffer_FUNC,
	_1gtk_1text_1view_1get_1editable_FUNC,
	_1gtk_1text_1view_1get_1iter_1at_1location_FUNC,
	_1gtk_1text_1view_1get_1iter_1location_FUNC,
	_1gtk_1text_1view_1get_1line_1at_1y_FUNC,
	_1gtk_1text_1view_1get_1visible_1rect_FUNC,
	_1gtk_1text_1view_1get_1window_FUNC,
	_1gtk_1text_1view_1new_FUNC,
	_1gtk_1text_1view_1scroll_1mark_1onscreen_FUNC,
	_1gtk_1text_1view_1scroll_1to_1iter_FUNC,
	_1gtk_1text_1view_1set_1editable_FUNC,
	_1gtk_1text_1view_1set_1justification_FUNC,
	_1gtk_1text_1view_1set_1tabs_FUNC,
	_1gtk_1text_1view_1set_1wrap_1mode_FUNC,
	_1gtk_1text_1view_1window_1to_1buffer_1coords_FUNC,
	_1gtk_1timeout_1add_FUNC,
	_1gtk_1timeout_1remove_FUNC,
	_1gtk_1toggle_1button_1get_1active_FUNC,
	_1gtk_1toggle_1button_1new_FUNC,
	_1gtk_1toggle_1button_1set_1active_FUNC,
	_1gtk_1toggle_1button_1set_1mode_FUNC,
	_1gtk_1toolbar_1insert_1widget_FUNC,
	_1gtk_1toolbar_1new_FUNC,
	_1gtk_1toolbar_1set_1orientation_FUNC,
	_1gtk_1tooltips_1disable_FUNC,
	_1gtk_1tooltips_1enable_FUNC,
	_1gtk_1tooltips_1new_FUNC,
	_1gtk_1tooltips_1set_1tip_FUNC,
	_1gtk_1tree_1model_1get__III_3II_FUNC,
	_1gtk_1tree_1model_1get__III_3JI_FUNC,
	_1gtk_1tree_1model_1get_1iter_FUNC,
	_1gtk_1tree_1model_1get_1iter_1first_FUNC,
	_1gtk_1tree_1model_1get_1n_1columns_FUNC,
	_1gtk_1tree_1model_1get_1path_FUNC,
	_1gtk_1tree_1model_1get_1type_FUNC,
	_1gtk_1tree_1model_1iter_1children_FUNC,
	_1gtk_1tree_1model_1iter_1n_1children_FUNC,
	_1gtk_1tree_1model_1iter_1next_FUNC,
	_1gtk_1tree_1model_1iter_1nth_1child_FUNC,
	_1gtk_1tree_1path_1append_1index_FUNC,
	_1gtk_1tree_1path_1compare_FUNC,
	_1gtk_1tree_1path_1down_FUNC,
	_1gtk_1tree_1path_1free_FUNC,
	_1gtk_1tree_1path_1get_1depth_FUNC,
	_1gtk_1tree_1path_1get_1indices_FUNC,
	_1gtk_1tree_1path_1new_FUNC,
	_1gtk_1tree_1path_1new_1first_FUNC,
	_1gtk_1tree_1path_1new_1from_1string__I_FUNC,
	_1gtk_1tree_1path_1new_1from_1string___3B_FUNC,
	_1gtk_1tree_1path_1next_FUNC,
	_1gtk_1tree_1path_1prev_FUNC,
	_1gtk_1tree_1path_1up_FUNC,
	_1gtk_1tree_1selection_1get_1selected_FUNC,
	_1gtk_1tree_1selection_1get_1selected_1rows_FUNC,
	_1gtk_1tree_1selection_1path_1is_1selected_FUNC,
	_1gtk_1tree_1selection_1select_1all_FUNC,
	_1gtk_1tree_1selection_1select_1iter_FUNC,
	_1gtk_1tree_1selection_1selected_1foreach_FUNC,
	_1gtk_1tree_1selection_1set_1mode_FUNC,
	_1gtk_1tree_1selection_1unselect_1all_FUNC,
	_1gtk_1tree_1selection_1unselect_1iter_FUNC,
	_1gtk_1tree_1store_1append_FUNC,
	_1gtk_1tree_1store_1clear_FUNC,
	_1gtk_1tree_1store_1insert_FUNC,
	_1gtk_1tree_1store_1newv_FUNC,
	_1gtk_1tree_1store_1remove_FUNC,
	_1gtk_1tree_1store_1set__IIIII_FUNC,
	_1gtk_1tree_1store_1set__IIIJI_FUNC,
	_1gtk_1tree_1store_1set__IIILorg_eclipse_swt_internal_gtk_GdkColor_2I_FUNC,
	_1gtk_1tree_1store_1set__IIIZI_FUNC,
	_1gtk_1tree_1store_1set__III_3BI_FUNC,
	_1gtk_1tree_1view_1collapse_1row_FUNC,
	_1gtk_1tree_1view_1column_1add_1attribute_FUNC,
	_1gtk_1tree_1view_1column_1cell_1get_1position_FUNC,
	_1gtk_1tree_1view_1column_1cell_1get_1size_FUNC,
	_1gtk_1tree_1view_1column_1cell_1set_1cell_1data_FUNC,
	_1gtk_1tree_1view_1column_1clear_FUNC,
	_1gtk_1tree_1view_1column_1get_1cell_1renderers_FUNC,
	_1gtk_1tree_1view_1column_1get_1fixed_1width_FUNC,
	_1gtk_1tree_1view_1column_1get_1reorderable_FUNC,
	_1gtk_1tree_1view_1column_1get_1resizable_FUNC,
	_1gtk_1tree_1view_1column_1get_1spacing_FUNC,
	_1gtk_1tree_1view_1column_1get_1visible_FUNC,
	_1gtk_1tree_1view_1column_1get_1width_FUNC,
	_1gtk_1tree_1view_1column_1new_FUNC,
	_1gtk_1tree_1view_1column_1pack_1end_FUNC,
	_1gtk_1tree_1view_1column_1pack_1start_FUNC,
	_1gtk_1tree_1view_1column_1set_1alignment_FUNC,
	_1gtk_1tree_1view_1column_1set_1cell_1data_1func_FUNC,
	_1gtk_1tree_1view_1column_1set_1clickable_FUNC,
	_1gtk_1tree_1view_1column_1set_1fixed_1width_FUNC,
	_1gtk_1tree_1view_1column_1set_1reorderable_FUNC,
	_1gtk_1tree_1view_1column_1set_1resizable_FUNC,
	_1gtk_1tree_1view_1column_1set_1sizing_FUNC,
	_1gtk_1tree_1view_1column_1set_1title_FUNC,
	_1gtk_1tree_1view_1column_1set_1visible_FUNC,
	_1gtk_1tree_1view_1column_1set_1widget_FUNC,
	_1gtk_1tree_1view_1expand_1row_FUNC,
	_1gtk_1tree_1view_1get_1bin_1window_FUNC,
	_1gtk_1tree_1view_1get_1cell_1area_FUNC,
	_1gtk_1tree_1view_1get_1column_FUNC,
	_1gtk_1tree_1view_1get_1columns_FUNC,
	_1gtk_1tree_1view_1get_1cursor_FUNC,
	_1gtk_1tree_1view_1get_1expander_1column_FUNC,
	_1gtk_1tree_1view_1get_1headers_1visible_FUNC,
	_1gtk_1tree_1view_1get_1path_1at_1pos_FUNC,
	_1gtk_1tree_1view_1get_1rules_1hint_FUNC,
	_1gtk_1tree_1view_1get_1selection_FUNC,
	_1gtk_1tree_1view_1get_1visible_1rect_FUNC,
	_1gtk_1tree_1view_1insert_1column_FUNC,
	_1gtk_1tree_1view_1move_1column_1after_FUNC,
	_1gtk_1tree_1view_1new_1with_1model_FUNC,
	_1gtk_1tree_1view_1remove_1column_FUNC,
	_1gtk_1tree_1view_1row_1expanded_FUNC,
	_1gtk_1tree_1view_1scroll_1to_1cell_FUNC,
	_1gtk_1tree_1view_1scroll_1to_1point_FUNC,
	_1gtk_1tree_1view_1set_1cursor_FUNC,
	_1gtk_1tree_1view_1set_1drag_1dest_1row_FUNC,
	_1gtk_1tree_1view_1set_1headers_1visible_FUNC,
	_1gtk_1tree_1view_1set_1model_FUNC,
	_1gtk_1tree_1view_1set_1rules_1hint_FUNC,
	_1gtk_1tree_1view_1tree_1to_1widget_1coords_FUNC,
	_1gtk_1tree_1view_1unset_1rows_1drag_1dest_FUNC,
	_1gtk_1tree_1view_1widget_1to_1tree_1coords_FUNC,
	_1gtk_1vbox_1new_FUNC,
	_1gtk_1vscale_1new_FUNC,
	_1gtk_1vscrollbar_1new_FUNC,
	_1gtk_1vseparator_1new_FUNC,
	_1gtk_1widget_1add_1accelerator_FUNC,
	_1gtk_1widget_1add_1events_FUNC,
	_1gtk_1widget_1child_1focus_FUNC,
	_1gtk_1widget_1create_1pango_1layout__II_FUNC,
	_1gtk_1widget_1create_1pango_1layout__I_3B_FUNC,
	_1gtk_1widget_1destroy_FUNC,
	_1gtk_1widget_1event_FUNC,
	_1gtk_1widget_1get_1child_1visible_FUNC,
	_1gtk_1widget_1get_1default_1direction_FUNC,
	_1gtk_1widget_1get_1default_1style_FUNC,
	_1gtk_1widget_1get_1direction_FUNC,
	_1gtk_1widget_1get_1events_FUNC,
	_1gtk_1widget_1get_1modifier_1style_FUNC,
	_1gtk_1widget_1get_1pango_1context_FUNC,
	_1gtk_1widget_1get_1parent_FUNC,
	_1gtk_1widget_1get_1size_1request_FUNC,
	_1gtk_1widget_1get_1style_FUNC,
	_1gtk_1widget_1get_1toplevel_FUNC,
	_1gtk_1widget_1grab_1focus_FUNC,
	_1gtk_1widget_1hide_FUNC,
	_1gtk_1widget_1is_1focus_FUNC,
	_1gtk_1widget_1map_FUNC,
	_1gtk_1widget_1mnemonic_1activate_FUNC,
	_1gtk_1widget_1modify_1base_FUNC,
	_1gtk_1widget_1modify_1bg_FUNC,
	_1gtk_1widget_1modify_1fg_FUNC,
	_1gtk_1widget_1modify_1font_FUNC,
	_1gtk_1widget_1modify_1style_FUNC,
	_1gtk_1widget_1modify_1text_FUNC,
	_1gtk_1widget_1realize_FUNC,
	_1gtk_1widget_1remove_1accelerator_FUNC,
	_1gtk_1widget_1reparent_FUNC,
	_1gtk_1widget_1set_1default_1direction_FUNC,
	_1gtk_1widget_1set_1direction_FUNC,
	_1gtk_1widget_1set_1double_1buffered_FUNC,
	_1gtk_1widget_1set_1name_FUNC,
	_1gtk_1widget_1set_1redraw_1on_1allocate_FUNC,
	_1gtk_1widget_1set_1sensitive_FUNC,
	_1gtk_1widget_1set_1size_1request_FUNC,
	_1gtk_1widget_1set_1state_FUNC,
	_1gtk_1widget_1shape_1combine_1mask_FUNC,
	_1gtk_1widget_1show_FUNC,
	_1gtk_1widget_1show_1now_FUNC,
	_1gtk_1widget_1size_1allocate_FUNC,
	_1gtk_1widget_1size_1request_FUNC,
	_1gtk_1widget_1style_1get__I_3B_3II_FUNC,
	_1gtk_1widget_1style_1get__I_3B_3JI_FUNC,
	_1gtk_1widget_1unrealize_FUNC,
	_1gtk_1window_1activate_1default_FUNC,
	_1gtk_1window_1add_1accel_1group_FUNC,
	_1gtk_1window_1deiconify_FUNC,
	_1gtk_1window_1get_1focus_FUNC,
	_1gtk_1window_1get_1mnemonic_1modifier_FUNC,
	_1gtk_1window_1get_1position_FUNC,
	_1gtk_1window_1get_1size_FUNC,
	_1gtk_1window_1iconify_FUNC,
	_1gtk_1window_1maximize_FUNC,
	_1gtk_1window_1move_FUNC,
	_1gtk_1window_1new_FUNC,
	_1gtk_1window_1present_FUNC,
	_1gtk_1window_1remove_1accel_1group_FUNC,
	_1gtk_1window_1resize_FUNC,
	_1gtk_1window_1set_1default_FUNC,
	_1gtk_1window_1set_1destroy_1with_1parent_FUNC,
	_1gtk_1window_1set_1geometry_1hints_FUNC,
	_1gtk_1window_1set_1modal_FUNC,
	_1gtk_1window_1set_1resizable_FUNC,
	_1gtk_1window_1set_1title_FUNC,
	_1gtk_1window_1set_1transient_1for_FUNC,
	_1gtk_1window_1set_1type_1hint_FUNC,
	_1gtk_1window_1unmaximize_FUNC,
	_1pango_1attr_1background_1new_FUNC,
	_1pango_1attr_1font_1desc_1new_FUNC,
	_1pango_1attr_1foreground_1new_FUNC,
	_1pango_1attr_1list_1change_FUNC,
	_1pango_1attr_1list_1insert_FUNC,
	_1pango_1attr_1list_1new_FUNC,
	_1pango_1attr_1list_1unref_FUNC,
	_1pango_1attr_1shape_1new_FUNC,
	_1pango_1attr_1strikethrough_1new_FUNC,
	_1pango_1attr_1underline_1new_FUNC,
	_1pango_1attr_1weight_1new_FUNC,
	_1pango_1context_1get_1base_1dir_FUNC,
	_1pango_1context_1get_1language_FUNC,
	_1pango_1context_1get_1metrics_FUNC,
	_1pango_1context_1list_1families_FUNC,
	_1pango_1context_1set_1base_1dir_FUNC,
	_1pango_1context_1set_1language_FUNC,
	_1pango_1font_1description_1copy_FUNC,
	_1pango_1font_1description_1free_FUNC,
	_1pango_1font_1description_1from_1string_FUNC,
	_1pango_1font_1description_1get_1family_FUNC,
	_1pango_1font_1description_1get_1size_FUNC,
	_1pango_1font_1description_1get_1style_FUNC,
	_1pango_1font_1description_1get_1weight_FUNC,
	_1pango_1font_1description_1new_FUNC,
	_1pango_1font_1description_1set_1family_FUNC,
	_1pango_1font_1description_1set_1size_FUNC,
	_1pango_1font_1description_1set_1stretch_FUNC,
	_1pango_1font_1description_1set_1style_FUNC,
	_1pango_1font_1description_1set_1weight_FUNC,
	_1pango_1font_1description_1to_1string_FUNC,
	_1pango_1font_1face_1describe_FUNC,
	_1pango_1font_1family_1get_1name_FUNC,
	_1pango_1font_1family_1list_1faces_FUNC,
	_1pango_1font_1get_1metrics_FUNC,
	_1pango_1font_1metrics_1get_1approximate_1char_1width_FUNC,
	_1pango_1font_1metrics_1get_1ascent_FUNC,
	_1pango_1font_1metrics_1get_1descent_FUNC,
	_1pango_1font_1metrics_1unref_FUNC,
	_1pango_1language_1from_1string_FUNC,
	_1pango_1layout_1context_1changed_FUNC,
	_1pango_1layout_1get_1alignment_FUNC,
	_1pango_1layout_1get_1attributes_FUNC,
	_1pango_1layout_1get_1iter_FUNC,
	_1pango_1layout_1get_1line_FUNC,
	_1pango_1layout_1get_1line_1count_FUNC,
	_1pango_1layout_1get_1log_1attrs_FUNC,
	_1pango_1layout_1get_1size_FUNC,
	_1pango_1layout_1get_1spacing_FUNC,
	_1pango_1layout_1get_1tabs_FUNC,
	_1pango_1layout_1get_1text_FUNC,
	_1pango_1layout_1get_1width_FUNC,
	_1pango_1layout_1index_1to_1pos_FUNC,
	_1pango_1layout_1iter_1free_FUNC,
	_1pango_1layout_1iter_1get_1index_FUNC,
	_1pango_1layout_1iter_1get_1line_1extents_FUNC,
	_1pango_1layout_1iter_1get_1run_FUNC,
	_1pango_1layout_1iter_1next_1line_FUNC,
	_1pango_1layout_1iter_1next_1run_FUNC,
	_1pango_1layout_1line_1get_1extents_FUNC,
	_1pango_1layout_1line_1x_1to_1index_FUNC,
	_1pango_1layout_1new_FUNC,
	_1pango_1layout_1set_1alignment_FUNC,
	_1pango_1layout_1set_1attributes_FUNC,
	_1pango_1layout_1set_1auto_1dir_FUNC,
	_1pango_1layout_1set_1font_1description_FUNC,
	_1pango_1layout_1set_1single_1paragraph_1mode_FUNC,
	_1pango_1layout_1set_1spacing_FUNC,
	_1pango_1layout_1set_1tabs_FUNC,
	_1pango_1layout_1set_1text_FUNC,
	_1pango_1layout_1set_1width_FUNC,
	_1pango_1layout_1set_1wrap_FUNC,
	_1pango_1layout_1xy_1to_1index_FUNC,
	_1pango_1tab_1array_1free_FUNC,
	_1pango_1tab_1array_1get_1size_FUNC,
	_1pango_1tab_1array_1get_1tabs_FUNC,
	_1pango_1tab_1array_1new_FUNC,
	_1pango_1tab_1array_1set_1tab_FUNC,
	g_1main_1context_1wakeup_FUNC,
	getenv_FUNC,
	localeconv_1decimal_1point_FUNC,
	memmove__III_FUNC,
	memmove__ILorg_eclipse_swt_internal_gtk_GInterfaceInfo_2I_FUNC,
	memmove__ILorg_eclipse_swt_internal_gtk_GObjectClass_2_FUNC,
	memmove__ILorg_eclipse_swt_internal_gtk_GTypeInfo_2I_FUNC,
	memmove__ILorg_eclipse_swt_internal_gtk_GdkEventButton_2I_FUNC,
	memmove__ILorg_eclipse_swt_internal_gtk_GtkAdjustment_2_FUNC,
	memmove__ILorg_eclipse_swt_internal_gtk_GtkFixed_2_FUNC,
	memmove__ILorg_eclipse_swt_internal_gtk_GtkTargetEntry_2I_FUNC,
	memmove__ILorg_eclipse_swt_internal_gtk_GtkWidgetClass_2_FUNC,
	memmove__ILorg_eclipse_swt_internal_gtk_PangoAttribute_2I_FUNC,
	memmove__ILorg_eclipse_swt_internal_gtk_XButtonEvent_2I_FUNC,
	memmove__ILorg_eclipse_swt_internal_gtk_XClientMessageEvent_2I_FUNC,
	memmove__ILorg_eclipse_swt_internal_gtk_XCrossingEvent_2I_FUNC,
	memmove__ILorg_eclipse_swt_internal_gtk_XExposeEvent_2I_FUNC,
	memmove__ILorg_eclipse_swt_internal_gtk_XFocusChangeEvent_2I_FUNC,
	memmove__I_3BI_FUNC,
	memmove__I_3CI_FUNC,
	memmove__I_3DI_FUNC,
	memmove__I_3II_FUNC,
	memmove__I_3JI_FUNC,
	memmove__Lorg_eclipse_swt_internal_gtk_GObjectClass_2I_FUNC,
	memmove__Lorg_eclipse_swt_internal_gtk_GTypeQuery_2II_FUNC,
	memmove__Lorg_eclipse_swt_internal_gtk_GdkColor_2II_FUNC,
	memmove__Lorg_eclipse_swt_internal_gtk_GdkDragContext_2II_FUNC,
	memmove__Lorg_eclipse_swt_internal_gtk_GdkEventButton_2II_FUNC,
	memmove__Lorg_eclipse_swt_internal_gtk_GdkEventCrossing_2II_FUNC,
	memmove__Lorg_eclipse_swt_internal_gtk_GdkEventExpose_2II_FUNC,
	memmove__Lorg_eclipse_swt_internal_gtk_GdkEventFocus_2II_FUNC,
	memmove__Lorg_eclipse_swt_internal_gtk_GdkEventKey_2II_FUNC,
	memmove__Lorg_eclipse_swt_internal_gtk_GdkEventMotion_2II_FUNC,
	memmove__Lorg_eclipse_swt_internal_gtk_GdkEventScroll_2II_FUNC,
	memmove__Lorg_eclipse_swt_internal_gtk_GdkEventVisibility_2II_FUNC,
	memmove__Lorg_eclipse_swt_internal_gtk_GdkEventWindowState_2II_FUNC,
	memmove__Lorg_eclipse_swt_internal_gtk_GdkEvent_2II_FUNC,
	memmove__Lorg_eclipse_swt_internal_gtk_GdkImage_2I_FUNC,
	memmove__Lorg_eclipse_swt_internal_gtk_GdkRectangle_2II_FUNC,
	memmove__Lorg_eclipse_swt_internal_gtk_GdkVisual_2I_FUNC,
	memmove__Lorg_eclipse_swt_internal_gtk_GtkAdjustment_2I_FUNC,
	memmove__Lorg_eclipse_swt_internal_gtk_GtkBorder_2II_FUNC,
	memmove__Lorg_eclipse_swt_internal_gtk_GtkColorSelectionDialog_2I_FUNC,
	memmove__Lorg_eclipse_swt_internal_gtk_GtkCombo_2I_FUNC,
	memmove__Lorg_eclipse_swt_internal_gtk_GtkFileSelection_2I_FUNC,
	memmove__Lorg_eclipse_swt_internal_gtk_GtkFixed_2I_FUNC,
	memmove__Lorg_eclipse_swt_internal_gtk_GtkSelectionData_2II_FUNC,
	memmove__Lorg_eclipse_swt_internal_gtk_GtkTargetPair_2II_FUNC,
	memmove__Lorg_eclipse_swt_internal_gtk_GtkWidgetClass_2I_FUNC,
	memmove__Lorg_eclipse_swt_internal_gtk_PangoAttribute_2II_FUNC,
	memmove__Lorg_eclipse_swt_internal_gtk_PangoItem_2II_FUNC,
	memmove__Lorg_eclipse_swt_internal_gtk_PangoLayoutLine_2II_FUNC,
	memmove__Lorg_eclipse_swt_internal_gtk_PangoLayoutRun_2II_FUNC,
	memmove__Lorg_eclipse_swt_internal_gtk_PangoLogAttr_2II_FUNC,
	memmove__Lorg_eclipse_swt_internal_gtk_XButtonEvent_2II_FUNC,
	memmove__Lorg_eclipse_swt_internal_gtk_XCrossingEvent_2II_FUNC,
	memmove__Lorg_eclipse_swt_internal_gtk_XExposeEvent_2II_FUNC,
	memmove__Lorg_eclipse_swt_internal_gtk_XFocusChangeEvent_2II_FUNC,
	memmove__Lorg_eclipse_swt_internal_gtk_XVisibilityEvent_2II_FUNC,
	memmove___3BII_FUNC,
	memmove___3CII_FUNC,
	memmove___3III_FUNC,
	memmove___3I_3BI_FUNC,
	memmove___3JII_FUNC,
	memset_FUNC,
	strlen_FUNC,
} OS_FUNCS;
