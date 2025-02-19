class SystemWindowMargin {
  /// Margin value of the left
  int? left;

  /// Margin value of the right
  int? right;

  /// Margin value of the top
  int? top;

  /// Margin value of the bottom
  int? bottom;

  SystemWindowMargin({this.left, this.right, this.top, this.bottom});

  /// Internal method to convert SystemWindowMargin to primitive dataTypes
  Map<String, int> getMap() {
    final Map<String, int> map = <String, int>{
      'left': left ?? 0,
      'right': right ?? 0,
      'top': top ?? 0,
      'bottom': bottom ?? 0,
    };
    return map;
  }

  /// Internal method to create symmetric margin across the axis
  static SystemWindowMargin setSymmetricMargin(int vertical, int horizontal) {
    return SystemWindowMargin(
        left: horizontal, right: horizontal, top: vertical, bottom: vertical);
  }
}
