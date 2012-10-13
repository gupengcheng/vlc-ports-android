/*****************************************************************************
 * jni_libvlc.h
 *****************************************************************************
 * Copyright (C) 2012~2014 Zhang Rui
 *
 * Author: Rui Zhang <bbcallen _AT_ gmail _DOT_ com>
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston MA 02110-1301, USA.
 *****************************************************************************/

#ifndef JNI_LIBVLC_H
#define JNI_LIBVLC_H

#include <stdint.h>
#include <string.h>
#include <jni.h>

/* Pointer to the Java virtual machine
 * Note: It's okay to use a static variable for the VM pointer since there
 * can only be one instance of this shared library in a single VM
 */
extern JavaVM *myVm;

extern jint SDL_AndroidJni_SetupThreadEnv(JNIEnv **p_env, void *thr_args);

#endif//JNI_LIBVLC_H
