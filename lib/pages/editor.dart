import 'package:flclashx/common/common.dart';
import 'package:flclashx/enum/enum.dart';
import 'package:flclashx/state.dart';
import 'package:flclashx/widgets/widgets.dart';
import 'package:flutter/material.dart';
import 'package:flutter_code_editor/flutter_code_editor.dart';
import 'package:flutter_highlight/themes/atom-one-dark.dart';
import 'package:flutter_highlight/themes/atom-one-light.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:highlight/languages/javascript.dart';
import 'package:highlight/languages/yaml.dart';

typedef TextEditingValueChangeBuilder = Widget Function(TextEditingValue value);

class EditorPage extends ConsumerStatefulWidget {

  const EditorPage({
    super.key,
    required this.title,
    required this.content,
    this.titleEditable = false,
    this.onSave,
    this.onPop,
    this.supportRemoteDownload = false,
    this.languages = const [
      Language.yaml,
    ],
  });
  final String title;
  final String content;
  final List<Language> languages;
  final bool supportRemoteDownload;
  final bool titleEditable;
  final Function(BuildContext context, String title, String content)? onSave;
  final Future<bool> Function(
      BuildContext context, String title, String content)? onPop;

  @override
  ConsumerState<EditorPage> createState() => _EditorPageState();
}

class _EditorPageState extends ConsumerState<EditorPage> {
  late CodeController _controller;
  late TextEditingController _titleController;
  bool _showSearch = false;
  final _searchController = TextEditingController();
  final _searchFocusNode = FocusNode();
  List<int> _searchMatches = [];
  int _currentMatch = -1;

  @override
  void initState() {
    super.initState();
    final lang = widget.languages.contains(Language.yaml) ? yaml
        : widget.languages.contains(Language.javaScript) ? javascript
        : null;
    _controller = CodeController(text: widget.content, language: lang);
    _titleController = TextEditingController(text: widget.title);
  }

  @override
  void dispose() {
    _controller.dispose();
    _titleController.dispose();
    _searchController.dispose();
    _searchFocusNode.dispose();
    super.dispose();
  }

  Widget _wrapTitleController(TextEditingValueChangeBuilder builder) =>
      ValueListenableBuilder(
        valueListenable: _titleController,
        builder: (_, value, ___) => builder(value),
      );

  void _handleSearch() {
    setState(() {
      _showSearch = !_showSearch;
      if (_showSearch) {
        Future.microtask(() => _searchFocusNode.requestFocus());
      } else {
        _searchMatches = [];
        _currentMatch = -1;
      }
    });
  }

  void _updateSearch(String query) {
    if (query.isEmpty) {
      setState(() {
        _searchMatches = [];
        _currentMatch = -1;
      });
      return;
    }
    final text = _controller.fullText.toLowerCase();
    final q = query.toLowerCase();
    final matches = <int>[];
    var start = 0;
    while (true) {
      final idx = text.indexOf(q, start);
      if (idx == -1) break;
      matches.add(idx);
      start = idx + 1;
    }
    setState(() {
      _searchMatches = matches;
      _currentMatch = matches.isNotEmpty ? 0 : -1;
    });
    _goToMatch();
  }

  void _nextMatch() {
    if (_searchMatches.isEmpty) return;
    setState(() {
      _currentMatch = (_currentMatch + 1) % _searchMatches.length;
    });
    _goToMatch();
  }

  void _prevMatch() {
    if (_searchMatches.isEmpty) return;
    setState(() {
      _currentMatch = (_currentMatch - 1 + _searchMatches.length) % _searchMatches.length;
    });
    _goToMatch();
  }

  void _goToMatch() {
    if (_currentMatch < 0 || _currentMatch >= _searchMatches.length) return;
    final pos = _searchMatches[_currentMatch];
    final len = _searchController.text.length;
    _controller.selection = TextSelection(baseOffset: pos, extentOffset: pos + len);
  }

  Future<void> _handleImport() async {
    final option = await globalState.showCommonDialog<ImportOption>(
      child: const _ImportOptionsDialog(),
    );
    if (option == null) return;
    if (option == ImportOption.file) {
      final file = await picker.pickerFile();
      if (file == null) return;
      final res = String.fromCharCodes(file.bytes?.toList() ?? []);
      _controller.fullText = res;
      return;
    }
    final url = await globalState.showCommonDialog(
      child: InputDialog(
        title: "导入",
        value: "",
        labelText: appLocalizations.url,
        validator: (value) {
          if (value == null || value.isEmpty) {
            return appLocalizations.emptyTip(appLocalizations.value);
          }
          if (!value.isUrl) {
            return appLocalizations.urlTip(appLocalizations.value);
          }
          return null;
        },
      ),
    );
    if (url == null) return;
    final res = await request.getTextResponseForUrl(url);
    _controller.fullText = res.data;
  }

  @override
  Widget build(BuildContext context) {
    return CommonPopScope(
      onPop: () async {
        if (widget.onPop == null) return true;
        final res = await widget.onPop!(
          context,
          _titleController.text,
          _controller.fullText,
        );
        if (res && context.mounted) return true;
        return false;
      },
      child: CommonScaffold(
        disableBackground: true,
        appBar: AppBar(
          title: TextField(
            enabled: widget.titleEditable,
            controller: _titleController,
            decoration: InputDecoration(
              border: const _NoInputBorder(),
              hintText: appLocalizations.unnamed,
            ),
            style: context.textTheme.titleLarge,
            autofocus: false,
          ),
          actions: genActions([
            if (widget.onSave != null)
              _wrapTitleController(
                (_) => IconButton(
                  onPressed: _controller.fullText != widget.content ||
                          _titleController.text != widget.title
                      ? () {
                          widget.onSave!(
                            context,
                            _titleController.text,
                            _controller.fullText,
                          );
                        }
                      : null,
                  icon: const Icon(Icons.save_sharp),
                ),
              ),
            if (widget.supportRemoteDownload)
              IconButton(
                onPressed: _handleImport,
                icon: const Icon(Icons.arrow_downward),
              ),
            IconButton(
              onPressed: _handleSearch,
              icon: const Icon(Icons.search),
            ),
          ]),
        ),
        body: Column(
          children: [
            if (_showSearch)
              _SearchBar(
                controller: _searchController,
                focusNode: _searchFocusNode,
                matchCount: _searchMatches.length,
                currentMatch: _currentMatch,
                onChanged: _updateSearch,
                onNext: _nextMatch,
                onPrev: _prevMatch,
                onClose: _handleSearch,
              ),
            Expanded(
              child: CodeTheme(
                data: CodeThemeData(
                  styles: Theme.of(context).brightness == Brightness.dark
                      ? atomOneDarkTheme
                      : atomOneLightTheme,
                ),
                child: CodeField(
                  controller: _controller,
                  wrap: true,
                  textStyle: TextStyle(
                    fontSize: context.textTheme.bodyLarge?.fontSize?.ap,
                    fontFamily: FontFamily.jetBrainsMono.value,
                  ),
                  gutterStyle: const GutterStyle(
                    showLineNumbers: true,
                    showFoldingHandles: true,
                    showErrors: false,
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _SearchBar extends StatelessWidget {
  const _SearchBar({
    required this.controller,
    required this.focusNode,
    required this.matchCount,
    required this.currentMatch,
    required this.onChanged,
    required this.onNext,
    required this.onPrev,
    required this.onClose,
  });

  final TextEditingController controller;
  final FocusNode focusNode;
  final int matchCount;
  final int currentMatch;
  final ValueChanged<String> onChanged;
  final VoidCallback onNext;
  final VoidCallback onPrev;
  final VoidCallback onClose;

  @override
  Widget build(BuildContext context) {
    final result = matchCount > 0
        ? '${currentMatch + 1}/$matchCount'
        : appLocalizations.none;
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      color: Theme.of(context).colorScheme.surface,
      child: Row(
        children: [
          Expanded(
            child: TextField(
              controller: controller,
              focusNode: focusNode,
              maxLines: 1,
              decoration: InputDecoration(
                isDense: true,
                contentPadding: const EdgeInsets.symmetric(
                    horizontal: 12, vertical: 8),
                border: const OutlineInputBorder(),
                hintText: appLocalizations.search,
              ),
              style: Theme.of(context).textTheme.bodyMedium,
              onChanged: onChanged,
              onSubmitted: (_) => onNext(),
            ),
          ),
          const SizedBox(width: 8),
          Text(result, style: Theme.of(context).textTheme.bodySmall),
          IconButton(
            icon: const Icon(Icons.arrow_upward, size: 16),
            visualDensity: VisualDensity.compact,
            onPressed: matchCount > 0 ? onPrev : null,
          ),
          IconButton(
            icon: const Icon(Icons.arrow_downward, size: 16),
            visualDensity: VisualDensity.compact,
            onPressed: matchCount > 0 ? onNext : null,
          ),
          IconButton(
            icon: const Icon(Icons.close, size: 16),
            visualDensity: VisualDensity.compact,
            onPressed: onClose,
          ),
        ],
      ),
    );
  }
}

class _NoInputBorder extends InputBorder {
  const _NoInputBorder() : super(borderSide: BorderSide.none);

  @override
  _NoInputBorder copyWith({BorderSide? borderSide}) => const _NoInputBorder();

  @override
  bool get isOutline => false;

  @override
  EdgeInsetsGeometry get dimensions => EdgeInsets.zero;

  @override
  _NoInputBorder scale(double t) => const _NoInputBorder();

  @override
  Path getInnerPath(Rect rect, {TextDirection? textDirection}) =>
      Path()..addRect(rect);

  @override
  Path getOuterPath(Rect rect, {TextDirection? textDirection}) =>
      Path()..addRect(rect);

  @override
  void paintInterior(Canvas canvas, Rect rect, Paint paint,
      {TextDirection? textDirection}) {
    canvas.drawRect(rect, paint);
  }

  @override
  bool get preferPaintInterior => true;

  @override
  void paint(
    Canvas canvas,
    Rect rect, {
    double? gapStart,
    double gapExtent = 0.0,
    double gapPercentage = 0.0,
    TextDirection? textDirection,
  }) {}
}

class _ImportOptionsDialog extends StatefulWidget {
  const _ImportOptionsDialog();

  @override
  State<_ImportOptionsDialog> createState() => _ImportOptionsDialogState();
}

class _ImportOptionsDialogState extends State<_ImportOptionsDialog> {
  void _handleOnTab(ImportOption value) {
    Navigator.of(context).pop(value);
  }

  @override
  Widget build(BuildContext context) => CommonDialog(
      title: appLocalizations.import,
      padding: const EdgeInsets.symmetric(
        horizontal: 8,
        vertical: 16,
      ),
      child: Wrap(
        children: [
          ListItem(
            onTap: () {
              _handleOnTab(ImportOption.url);
            },
            title: Text(appLocalizations.importUrl),
          ),
          ListItem(
            onTap: () {
              _handleOnTab(ImportOption.file);
            },
            title: Text(appLocalizations.importFile),
          )
        ],
      ),
    );
}
