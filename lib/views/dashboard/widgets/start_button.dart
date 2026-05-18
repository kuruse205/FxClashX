import 'dart:ui' show FontFeature;

import 'package:flclashx/common/common.dart';
import 'package:flclashx/providers/providers.dart';
import 'package:flclashx/state.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

class StartButton extends ConsumerStatefulWidget {
  const StartButton({super.key});

  @override
  ConsumerState<StartButton> createState() => _StartButtonState();
}

class _StartButtonState extends ConsumerState<StartButton>
    with TickerProviderStateMixin {
  late AnimationController _controller;
  late AnimationController _pressController;
  late Animation<double> _animation;
  late Animation<double> _scaleAnimation;
  bool isStart = false;
  bool _isSwitching = false;

  @override
  void initState() {
    super.initState();
    isStart = globalState.appState.runTime != null;
    _controller = AnimationController(
      vsync: this,
      value: isStart ? 1 : 0,
      duration: const Duration(milliseconds: 300),
    );
    _animation = CurvedAnimation(
      parent: _controller,
      curve: Curves.easeOutCubic,
    );

    _pressController = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 150),
    );
    _scaleAnimation = Tween<double>(begin: 1.0, end: 0.95).animate(
      CurvedAnimation(
        parent: _pressController,
        curve: Curves.easeOut,
      ),
    );

    ref.listenManual(
      runTimeProvider.select((state) => state != null),
      (prev, next) {
        if (next != isStart) {
          isStart = next;
          updateController();
        }
      },
      fireImmediately: true,
    );
  }

  @override
  void dispose() {
    _controller.dispose();
    _pressController.dispose();
    super.dispose();
  }

  Future<void> handleSwitchStart() async {
    if (_isSwitching) {
      return;
    }
    _isSwitching = true;
    final nextIsStart = !isStart;
    isStart = nextIsStart;
    updateController();
    try {
      await globalState.appController.updateStatus(nextIsStart);
    } catch (_) {
      isStart = !nextIsStart;
      updateController();
      rethrow;
    } finally {
      _isSwitching = false;
    }
  }

  void updateController() {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (isStart) {
        _controller.forward();
      } else {
        _controller.reverse();
      }
    });
  }

  void _onTapDown(TapDownDetails details) {
    _pressController.forward();
  }

  void _onTapUp(TapUpDetails details) {
    _pressController.reverse();
  }

  void _onTapCancel() {
    _pressController.reverse();
  }

  @override
  Widget build(BuildContext context) {
    final state = ref.watch(startButtonSelectorStateProvider);
    if (!state.isInit || !state.hasProfile) {
      return Container();
    }

    final colorScheme = Theme.of(context).colorScheme;
    final activeColor = Colors.green.shade600.withValues(alpha: 0.9);
    final inactiveColor =
        colorScheme.secondaryContainer.withValues(alpha: 0.85);

    return Padding(
      padding: const EdgeInsets.all(16.0),
      child: AnimatedBuilder(
        animation: Listenable.merge([_controller, _pressController]),
        builder: (_, child) {
          final progress = _animation.value;
          final foregroundColor = Color.lerp(
            colorScheme.onSecondaryContainer,
            Colors.white,
            progress,
          )!;

          return Transform.scale(
            scale: _scaleAnimation.value,
            child: SizedBox(
              width: double.infinity,
              height: 56,
              child: GestureDetector(
                onTapDown: _onTapDown,
                onTapUp: _onTapUp,
                onTapCancel: _onTapCancel,
                child: FilledButton(
                  onPressed: handleSwitchStart,
                  style: FilledButton.styleFrom(
                    backgroundColor:
                        Color.lerp(inactiveColor, activeColor, progress),
                    foregroundColor: foregroundColor,
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(12),
                    ),
                    elevation: 4 * progress,
                  ),
                  child: Stack(
                    alignment: Alignment.center,
                    children: [
                      Opacity(
                        opacity: (1 - progress).clamp(0, 1),
                        child: AnimatedIcon(
                          icon: AnimatedIcons.play_pause,
                          progress: _animation,
                          size: 36,
                          color: foregroundColor,
                        ),
                      ),
                      Opacity(
                        opacity: progress.clamp(0, 1),
                        child: Transform.translate(
                          offset: const Offset(-8, 0),
                          child: Row(
                            mainAxisSize: MainAxisSize.min,
                            children: [
                              AnimatedIcon(
                                icon: AnimatedIcons.play_pause,
                                progress: _animation,
                                size: 36,
                                color: foregroundColor,
                              ),
                              Padding(
                                padding: const EdgeInsets.only(left: 12),
                                child: ConstrainedBox(
                                  constraints:
                                      const BoxConstraints(maxWidth: 160),
                                  child: FittedBox(
                                    fit: BoxFit.scaleDown,
                                    child: child,
                                  ),
                                ),
                              ),
                            ],
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            ),
          );
        },
        child: Consumer(
          builder: (_, ref, __) {
            final runTime = ref.watch(runTimeProvider);
            final text = utils.getTimeText(runTime);
            return Text(
              text,
              maxLines: 1,
              softWrap: false,
              textAlign: TextAlign.center,
              style: context.textTheme.titleMedium?.toSoftBold.copyWith(
                color: Colors.white,
                fontFeatures: const [FontFeature.tabularFigures()],
              ),
            );
          },
        ),
      ),
    );
  }
}
