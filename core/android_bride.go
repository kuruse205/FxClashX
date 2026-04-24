//go:build android && cgo

package main

/*
#include <stdlib.h>

typedef void (*release_object_func)(void *obj);

typedef void (*protect_func)(void *tun_interface, int fd);

typedef const char* (*resolve_process_func)(void *tun_interface, int protocol, const char *source, const char *target, int uid);

typedef void (*invoke_callback_func)(void *callback, const char *data);

typedef void (*event_listener_func)(void *listener, const char *data);

static void protect(protect_func fn, void *tun_interface, int fd) {
    if (fn) {
        fn(tun_interface, fd);
    }
}

static const char* resolve_process(resolve_process_func fn, void *tun_interface, int protocol, const char *source, const char *target, int uid) {
    if (fn) {
        return fn(tun_interface, protocol, source, target, uid);
    }
    return "";
}

static void release_object(release_object_func fn, void *obj) {
    if (fn) {
        return fn(obj);
    }
}

static void invoke_callback(invoke_callback_func fn, void *callback, const char *data) {
    if (fn) {
        fn(callback, data);
    }
}

static void dispatch_event(event_listener_func fn, void *listener, const char *data) {
    if (fn) {
        fn(listener, data);
    }
}
*/
import "C"
import (
	"sync"
	"unsafe"
)

var (
	globalCallbacks struct {
		releaseObjectFunc  C.release_object_func
		protectFunc        C.protect_func
		resolveProcessFunc C.resolve_process_func
		invokeCallbackFunc C.invoke_callback_func
		eventListenerFunc  C.event_listener_func
	}

	eventListenerMu sync.Mutex
	eventListener   unsafe.Pointer
)

func Protect(callback unsafe.Pointer, fd int) {
	if globalCallbacks.protectFunc != nil {
		C.protect(globalCallbacks.protectFunc, callback, C.int(fd))
	}
}

func ResolveProcess(callback unsafe.Pointer, protocol int, source, target string, uid int) string {
	if globalCallbacks.resolveProcessFunc == nil {
		return ""
	}
	s := C.CString(source)
	defer C.free(unsafe.Pointer(s))
	t := C.CString(target)
	defer C.free(unsafe.Pointer(t))
	res := C.resolve_process(globalCallbacks.resolveProcessFunc, callback, C.int(protocol), s, t, C.int(uid))
	defer C.free(unsafe.Pointer(res))
	return C.GoString(res)
}

func releaseObject(callback unsafe.Pointer) {
	if globalCallbacks.releaseObjectFunc == nil {
		return
	}
	C.release_object(globalCallbacks.releaseObjectFunc, callback)
}

// invokeCallback delivers a JSON-encoded ActionResult back to the Kotlin
// side through the registered invoke_callback_func. The callback pointer is
// the caller-provided binder object; it must have been acquired with a
// retain/strong reference on the JVM side so it outlives this call, and
// release is the caller's responsibility.
func invokeCallback(callback unsafe.Pointer, data string) {
	if globalCallbacks.invokeCallbackFunc == nil || callback == nil {
		return
	}
	s := C.CString(data)
	defer C.free(unsafe.Pointer(s))
	C.invoke_callback(globalCallbacks.invokeCallbackFunc, callback, s)
}

// emitEvent pushes an out-of-band message (log line, delay update, request)
// to the currently registered event listener. Events are dropped silently
// when no listener is attached.
func emitEvent(data string) {
	eventListenerMu.Lock()
	listener := eventListener
	eventListenerMu.Unlock()
	if globalCallbacks.eventListenerFunc == nil || listener == nil {
		return
	}
	s := C.CString(data)
	defer C.free(unsafe.Pointer(s))
	C.dispatch_event(globalCallbacks.eventListenerFunc, listener, s)
}

//export registerCallbacks
func registerCallbacks(
	invokeCallbackFunc C.invoke_callback_func,
	eventListenerFunc C.event_listener_func,
	markSocketFunc C.protect_func,
	resolveProcessFunc C.resolve_process_func,
	releaseObjectFunc C.release_object_func,
) {
	globalCallbacks.invokeCallbackFunc = invokeCallbackFunc
	globalCallbacks.eventListenerFunc = eventListenerFunc
	globalCallbacks.protectFunc = markSocketFunc
	globalCallbacks.resolveProcessFunc = resolveProcessFunc
	globalCallbacks.releaseObjectFunc = releaseObjectFunc
}

//export setEventListener
func setEventListener(listener unsafe.Pointer) {
	eventListenerMu.Lock()
	prev := eventListener
	eventListener = listener
	eventListenerMu.Unlock()
	if prev != nil {
		releaseObject(prev)
	}
}
